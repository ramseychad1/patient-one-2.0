package com.hubaccess.domain.outreach.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOutreachRequest(
        @NotBlank String outreachType,
        @NotBlank String channel,
        String messageOverride
) {}
