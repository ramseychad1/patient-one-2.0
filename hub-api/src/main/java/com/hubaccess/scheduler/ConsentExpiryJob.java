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
public class ConsentExpiryJob {

    private final PatientOutreachRepository outreachRepo;
    private final CaseTaskRepository taskRepo;

    @Scheduled(cron = "0 0 6 * * *") // daily at 6am
    @Transactional
    public void checkConsentExpiry() {
        outreachRepo.findAll().stream()
                .filter(o -> "CONSENT_REQUEST".equals(o.getOutreachType()))
                .filter(o -> "SENT".equals(o.getDeliveryStatus()))
                .filter(o -> o.getAccessCodeExpiresAt() != null && o.getAccessCodeExpiresAt().isBefore(OffsetDateTime.now()))
                .forEach(o -> {
                    taskRepo.save(CaseTask.builder()
                            .caseId(o.getCaseId())
                            .taskType("SEND_CONSENT")
                            .title("Re-send consent — link expired")
                            .description("Consent portal link expired. Re-send consent request to patient.")
                            .priority("HIGH")
                            .actionKey("SEND_CONSENT_SMS")
                            .dueAt(OffsetDateTime.now().plusDays(1))
                            .build());
                    log.info("Consent link expired for case outreach {}", o.getId());
                });
    }
}
