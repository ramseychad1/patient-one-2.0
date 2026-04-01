package com.hubaccess.domain.childcase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MissingInformationDto(
        UUID id,
        UUID hubCaseId,
        String miNumber,
        String category,
        String detail,
        String miType,
        LocalDate reportedDate,
        LocalDate receivedDate,
        OffsetDateTime createdAt
) {}
