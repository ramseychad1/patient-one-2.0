package com.hubaccess.domain.activity;

import com.hubaccess.domain.activity.dto.CaseTaskDto;
import com.hubaccess.domain.activity.dto.CreateTaskRequest;
import com.hubaccess.domain.activity.dto.PatchTaskRequest;
import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.HubCaseRepository;
import com.hubaccess.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseTaskService {

    private final CaseTaskRepository taskRepo;
    private final HubCaseRepository caseRepo;

    private static final List<String> PRIORITY_ORDER = List.of("HIGH", "MEDIUM", "LOW");

    @Transactional(readOnly = true)
    public List<CaseTaskDto> listTasks(UUID caseId) {
        return taskRepo.findByCaseId(caseId).stream()
                .sorted(Comparator
                        .comparing((CaseTask t) -> t.getStatus().equals("OPEN") || t.getStatus().equals("IN_PROGRESS") ? 0 : 1)
                        .thenComparing(t -> PRIORITY_ORDER.indexOf(t.getPriority()))
                        .thenComparing(t -> t.getDueAt() != null ? t.getDueAt() : java.time.OffsetDateTime.MAX))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CaseTaskDto createTask(UUID caseId, CreateTaskRequest req, AuthenticatedUser user) {
        CaseTask task = CaseTask.builder()
                .caseId(caseId)
                .taskType("MANUAL")
                .title(req.title())
                .description(req.description())
                .priority(req.priority() != null ? req.priority() : "MEDIUM")
                .dueAt(req.dueDate())
                .assignedTo(req.assignedToId() != null ? req.assignedToId() : user.id())
                .createdBy(user.id())
                .build();
        taskRepo.save(task);
        return toDto(task);
    }

    @Transactional
    public CaseTaskDto patchTask(UUID caseId, UUID taskId, PatchTaskRequest req, AuthenticatedUser user) {
        CaseTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + taskId));

        if (req.title() != null) task.setTitle(req.title());
        if (req.description() != null) task.setDescription(req.description());
        if (req.priority() != null) task.setPriority(req.priority());
        if (req.dueDate() != null) task.setDueAt(req.dueDate());
        if (req.assignedToId() != null) task.setAssignedTo(req.assignedToId());
        if (req.completionNotes() != null) task.setCompletionNotes(req.completionNotes());
        if (req.completedAt() != null) {
            task.setCompletedAt(req.completedAt());
            task.setCompletedBy(user.id());
            task.setStatus("COMPLETED");
        }

        taskRepo.save(task);
        return toDto(task);
    }

    @Transactional(readOnly = true)
    public List<CaseTaskDto> getMyTasks(AuthenticatedUser user) {
        List<CaseTask> tasks;
        if (user.roles().contains("HUB_ADMIN")) {
            // Admin sees all open tasks across all users
            tasks = taskRepo.findAll().stream()
                    .filter(t -> List.of("OPEN", "IN_PROGRESS").contains(t.getStatus()))
                    .toList();
        } else {
            tasks = taskRepo.findByAssignedToAndStatusIn(user.id(), List.of("OPEN", "IN_PROGRESS"));
        }
        // Filter by active program if set
        if (user.activeProgramId() != null) {
            Set<UUID> programCaseIds = caseRepo.findAll().stream()
                    .filter(c -> c.getProgramId().equals(user.activeProgramId()))
                    .map(HubCase::getId)
                    .collect(Collectors.toSet());
            tasks = tasks.stream().filter(t -> programCaseIds.contains(t.getCaseId())).toList();
        }

        return tasks.stream()
                .sorted(Comparator
                        .comparing((CaseTask t) -> PRIORITY_ORDER.indexOf(t.getPriority()))
                        .thenComparing(t -> t.getDueAt() != null ? t.getDueAt() : java.time.OffsetDateTime.MAX))
                .map(this::toDto)
                .toList();
    }

    private CaseTaskDto toDto(CaseTask t) {
        return new CaseTaskDto(t.getId(), t.getCaseId(), t.getTaskType(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getPriority(), t.getActionKey(), t.getSlaBreached(), t.getDueAt(), t.getCreatedAt());
    }
}
