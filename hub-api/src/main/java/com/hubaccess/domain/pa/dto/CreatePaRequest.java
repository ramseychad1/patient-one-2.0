package com.hubaccess.domain.pa.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePaRequest(
        @NotBlank String payerName,
        String payerFax,
        String payerPhone,
        @NotBlank String paType
) {}
