package com.hubaccess.scheduler;

import com.hubaccess.domain.activity.CaseTask;
import com.hubaccess.domain.activity.CaseTaskRepository;
import com.hubaccess.domain.outreach.PatientOutreach;
import com.hubaccess.domain.outreach.PatientOutreachRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutreachRetryJob {

    private final PatientOutreachRepository outreachRepo;
    private final CaseTaskRepository taskRepo;

    @Scheduled(fixedDelay = 14_400_000) // every 4 hours
    @Transactional
    public void retryFailedOutreach() {
        outreachRepo.findAll().stream()
                .filter(o -> "FAILED".equals(o.getDeliveryStatus())
                        || ("SENT".equals(o.getDeliveryStatus())
                            && o.getCreatedAt().plusHours(48).isBefore(OffsetDateTime.now())
                            && o.getResolvedAt() == null))
                .forEach(o -> {
                    taskRepo.save(CaseTask.builder()
                            .caseId(o.getCaseId())
                            .taskType("SEND_CONSENT")
                            .title("Manual outreach needed — automated delivery failed")
                            .description("Outreach " + o.getOutreachType() + " via " + o.getChannel() + " failed or unresolved after 48h.")
                            .priority("HIGH")
                            .dueAt(OffsetDateTime.now().plusDays(1))
                            .build());
                    log.info("Outreach retry task created for outreach {}", o.getId());
                });
    }
}
