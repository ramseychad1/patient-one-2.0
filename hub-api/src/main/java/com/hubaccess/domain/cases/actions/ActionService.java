package com.hubaccess.domain.cases.actions;

import com.hubaccess.domain.activity.*;
import com.hubaccess.domain.cases.CaseService;
import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.HubCaseRepository;
import com.hubaccess.domain.cases.dto.ActionResultDto;
import com.hubaccess.domain.cases.dto.CaseDetailDto;
import com.hubaccess.domain.workflow.WorkflowState;
import com.hubaccess.domain.workflow.WorkflowStateRepository;
import com.hubaccess.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActionService {

    private final HubCaseRepository caseRepo;
    private final WorkflowStateRepository workflowStateRepo;
    private final CaseStatusHistoryRepository statusHistoryRepo;
    private final ServiceCallLogRepository serviceCallLogRepo;
    private final CaseService caseService;

    @Value("${demo.scenario:DEFAULT}")
    private String demoScenario;

    private final Map<String, ActionHandler> handlers = new LinkedHashMap<>();

    public void registerHandler(String actionKey, ActionHandler handler) {
        handlers.put(actionKey, handler);
    }

    @Transactional
    public ActionResultDto performAction(UUID caseId, String actionKey,
                                          Map<String, Object> payload, AuthenticatedUser user) {
        HubCase hc = caseRepo.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        ActionHandler handler = handlers.get(actionKey);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown action key: " + actionKey);
        }

        String prevState = hc.getCurrentWorkflowState();
        String prevStage = hc.getCurrentStage();

        // Execute the action (may call stub services)
        long start = System.currentTimeMillis();
        ActionResultDto.StubResult stubResult = handler.execute(hc, payload != null ? payload : Map.of(), user);
        long latencyMs = System.currentTimeMillis() - start;

        // Determine new state
        String newState = handler.getNextState(hc);
        String newStage = handler.getNextStage(hc);
        String nextActionKey = handler.getNextActionKey(hc);
        String nextActionLabel = handler.getNextActionLabel(hc);

        // Update case
        if (newState != null) hc.setCurrentWorkflowState(newState);
        if (newStage != null) hc.setCurrentStage(newStage);

        // Clear SLA breach flag when the breaching condition is resolved
        if (hc.getSlaBreachFlag() && newState != null
                && !List.of("PA_PENDING", "PA_SUBMITTED").contains(newState)) {
            hc.setSlaBreachFlag(false);
            hc.setEscalationFlag(false);
            hc.setEscalationReason(null);
        }

        caseRepo.save(hc);

        // Update workflow state
        if (newState != null) {
            workflowStateRepo.markCurrentExited(caseId);
            workflowStateRepo.save(WorkflowState.builder()
                    .caseId(caseId)
                    .state(newState)
                    .enteredAt(OffsetDateTime.now())
                    .triggeredByAction(actionKey)
                    .triggeredByUser(user.id())
                    .triggeredByStub(stubResult != null ? stubResult.serviceName() : null)
                    .nextRequiredAction(nextActionKey)
                    .nextActionLabel(nextActionLabel)
                    .isCurrent(true)
                    .build());
        }

        // Log status history
        statusHistoryRepo.save(CaseStatusHistory.builder()
                .caseId(caseId)
                .fromState(prevState)
                .toState(newState != null ? newState : prevState)
                .fromStage(prevStage)
                .toStage(newStage != null ? newStage : prevStage)
                .triggeredByAction(actionKey)
                .changedBy(user.id())
                .changedAt(OffsetDateTime.now())
                .build());

        // Log service call
        if (stubResult != null) {
            serviceCallLogRepo.save(ServiceCallLog.builder()
                    .caseId(caseId)
                    .serviceName(stubResult.serviceName())
                    .methodName(actionKey)
                    .isStub(true)
                    .latencyMs((int) latencyMs)
                    .success(true)
                    .demoScenario(demoScenario)
                    .calledAt(OffsetDateTime.now())
                    .calledByUser(user.id())
                    .build());
        }

        CaseDetailDto updatedCase = caseService.getCaseDetail(caseId);

        return new ActionResultDto(
                actionKey, true,
                "Action " + actionKey + " completed successfully",
                stubResult, nextActionKey, nextActionLabel, updatedCase
        );
    }

    public String getDemoScenario() {
        // X-Demo-Scenario header takes precedence over env var
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String header = attrs.getRequest().getHeader("X-Demo-Scenario");
                if (header != null && !header.isBlank()) return header;
            }
        } catch (Exception ignored) {}
        return demoScenario;
    }
}
