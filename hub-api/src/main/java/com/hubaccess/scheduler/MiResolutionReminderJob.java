package com.hubaccess.scheduler;

import com.hubaccess.domain.activity.CaseTask;
import com.hubaccess.domain.activity.CaseTaskRepository;
import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.HubCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MiResolutionReminderJob {

    private final HubCaseRepository caseRepo;
    private final CaseTaskRepository taskRepo;

    @Scheduled(cron = "0 0 8 * * *") // daily at 8am
    @Transactional
    public void checkMiResolution() {
        caseRepo.findAll().stream()
                .filter(c -> "INTAKE_PENDING".equals(c.getCurrentWorkflowState()))
                .filter(c -> c.getCreatedAt().plusDays(3).isBefore(OffsetDateTime.now()))
                .forEach(hc -> {
                    boolean hasOpenMiTask = taskRepo.findByCaseId(hc.getId()).stream()
                            .anyMatch(t -> "RESOLVE_MI".equals(t.getTaskType()) && "OPEN".equals(t.getStatus()));
                    if (hasOpenMiTask) {
                        taskRepo.save(CaseTask.builder()
                                .caseId(hc.getId())
                                .taskType("RESOLVE_MI")
                                .title("Follow up on MI request — no response")
                                .description("MI request sent 3+ days ago with no resolution. Follow up with patient/HCP.")
                                .priority("HIGH")
                                .assignedTo(hc.getAssignedCmId())
                                .dueAt(OffsetDateTime.now().plusDays(1))
                                .build());
                        log.info("MI follow-up task created for case {}", hc.getCaseNumber());
                    }
                });
    }
}
