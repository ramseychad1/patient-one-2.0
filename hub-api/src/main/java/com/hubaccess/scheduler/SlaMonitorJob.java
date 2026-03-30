package com.hubaccess.scheduler;

import com.hubaccess.domain.activity.CaseStatusHistory;
import com.hubaccess.domain.activity.CaseStatusHistoryRepository;
import com.hubaccess.domain.activity.Interaction;
import com.hubaccess.domain.activity.InteractionRepository;
import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.HubCaseRepository;
import com.hubaccess.domain.program.ProgramConfig;
import com.hubaccess.domain.program.ProgramConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaMonitorJob {

    private final HubCaseRepository caseRepo;
    private final ProgramConfigRepository configRepo;
    private final InteractionRepository interactionRepo;
    private final CaseStatusHistoryRepository statusHistoryRepo;

    @Scheduled(fixedDelay = 900_000) // every 15 minutes
    @Transactional
    public void checkSlaBreaches() {
        List<HubCase> paCases = caseRepo.findAll().stream()
                .filter(c -> "PA".equals(c.getCurrentStage()))
                .filter(c -> !c.getSlaBreachFlag())
                .filter(c -> List.of("PA_PENDING", "PA_SUBMITTED").contains(c.getCurrentWorkflowState()))
                .toList();

        for (HubCase hc : paCases) {
            ProgramConfig config = configRepo.findByProgramId(hc.getProgramId()).orElse(null);
            int slaDays = config != null && config.getPaSubmitSlaBusinessDays() != null
                    ? config.getPaSubmitSlaBusinessDays() : 3;

            if (hc.getCreatedAt().plusDays(slaDays).isBefore(OffsetDateTime.now())) {
                hc.setSlaBreachFlag(true);
                caseRepo.save(hc);

                interactionRepo.save(Interaction.builder()
                        .caseId(hc.getId())
                        .interactionType("SYSTEM_EVENT")
                        .direction("INTERNAL")
                        .channel("SYSTEM")
                        .subject("PA SLA breached")
                        .body("PA submission SLA of " + slaDays + " business days breached.")
                        .performedBy(hc.getAssignedCmId())
                        .build());

                if (config != null && Boolean.TRUE.equals(config.getPaAutoEscalate())) {
                    hc.setEscalationFlag(true);
                    hc.setEscalationReason("PA SLA breached — auto-escalated");
                    caseRepo.save(hc);
                }

                log.info("SLA breach flagged for case {}", hc.getCaseNumber());
            }
        }
    }
}
