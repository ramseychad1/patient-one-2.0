package com.hubaccess.domain.cases.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActionResultDto(
        String actionKey,
        boolean success,
        String message,
        StubResult stubResult,
        String nextActionKey,
        String nextActionLabel,
        CaseDetailDto caseUpdated
) {
    public record StubResult(
            String serviceName,
            Integer latencyMs,
            Map<String, String> fields
    ) {}
}
