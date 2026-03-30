package com.hubaccess.domain.pa.dto;

public record PatchPaRequest(
        String status,
        String authorizationNumber,
        String denialReason,
        String denialCode
) {}
