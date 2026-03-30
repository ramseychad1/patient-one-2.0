package com.hubaccess.domain.activity;

import com.hubaccess.domain.activity.dto.CreateInteractionRequest;
import com.hubaccess.domain.activity.dto.InteractionDto;
import com.hubaccess.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepo;

    @Transactional(readOnly = true)
    public List<InteractionDto> listInteractions(UUID caseId, int limit) {
        return interactionRepo.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .limit(limit)
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public InteractionDto createInteraction(UUID caseId, CreateInteractionRequest req, AuthenticatedUser user) {
        String channel = switch (req.interactionType()) {
            case "CALL" -> "PHONE";
            case "EMAIL_SENT" -> "EMAIL";
            case "FAX_SENT" -> "FAX";
            case "SMS_SENT" -> "SMS";
            default -> "SYSTEM";
        };

        Interaction interaction = Interaction.builder()
                .caseId(caseId)
                .interactionType(req.interactionType())
                .direction(req.direction() != null ? req.direction() : "OUTBOUND")
                .channel(channel)
                .contactName(req.contactName())
                .subject(req.interactionType() + " — " + (req.contactName() != null ? req.contactName() : ""))
                .body(req.notes())
                .durationMinutes(req.durationMinutes())
                .performedBy(user.id())
                .build();
        interactionRepo.save(interaction);
        return toDto(interaction);
    }

    private InteractionDto toDto(Interaction i) {
        return new InteractionDto(i.getId(), i.getInteractionType(), i.getDirection(), i.getChannel(),
                i.getContactName(), i.getContactRole(), i.getSubject(), i.getBody(),
                i.getDurationMinutes(), i.getAdverseEventReported(), i.getCreatedAt());
    }
}
