package com.hubaccess.domain.activity.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateInteractionRequest(
        @NotBlank String interactionType,
        String direction,
        Integer durationMinutes,
        String notes,
        String contactName
) {}
