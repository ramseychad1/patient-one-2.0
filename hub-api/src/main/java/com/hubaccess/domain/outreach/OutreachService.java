package com.hubaccess.domain.outreach;

import com.hubaccess.domain.outreach.dto.CreateOutreachRequest;
import com.hubaccess.domain.outreach.dto.PatientOutreachDto;
import com.hubaccess.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OutreachService {

    private final PatientOutreachRepository outreachRepo;

    @Transactional(readOnly = true)
    public List<PatientOutreachDto> listOutreach(UUID caseId) {
        return outreachRepo.findByCaseId(caseId).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PatientOutreachDto createOutreach(UUID caseId, CreateOutreachRequest req, AuthenticatedUser user) {
        String accessCode = generateAccessCode();
        String smsId = "SMS-" + UUID.randomUUID().toString().substring(0, 8);
        String consentUrl = "CONSENT_REQUEST".equals(req.outreachType())
                ? "https://consent.hubaccess.demo/" + caseId + "?code=" + accessCode
                : null;

        String message = req.messageOverride() != null ? req.messageOverride()
                : generateDefaultMessage(req.outreachType(), consentUrl);

        PatientOutreach outreach = PatientOutreach.builder()
                .caseId(caseId)
                .outreachType(req.outreachType())
                .channel(req.channel())
                .recipientType("PATIENT")
                .messageBody(message)
                .uniqueUrl(consentUrl)
                .accessCode(accessCode)
                .accessCodeExpiresAt(OffsetDateTime.now().plusDays(7))
                .stubMessageId(smsId)
                .deliveryStatus("MOCK")
                .initiatedBy(user.id())
                .build();
        outreachRepo.save(outreach);
        return toDto(outreach);
    }

    private String generateAccessCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateDefaultMessage(String type, String consentUrl) {
        return switch (type) {
            case "CONSENT_REQUEST" -> "Please complete your consent form: " + consentUrl;
            case "MI_REQUEST" -> "We need additional information for your enrollment. Please call us at 800-555-0100.";
            case "REFILL_REMINDER" -> "Your medication refill is coming up soon. Please contact your pharmacy.";
            default -> "Message from HubAccess patient services.";
        };
    }

    private PatientOutreachDto toDto(PatientOutreach o) {
        return new PatientOutreachDto(o.getId(), o.getOutreachType(), o.getChannel(), o.getRecipientType(),
                o.getDeliveryStatus(), o.getStubMessageId(), o.getMessageBody(), o.getUniqueUrl(),
                o.getAccessCode(), o.getCreatedAt(), o.getResolvedAt());
    }
}
