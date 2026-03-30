package com.hubaccess.domain.cases.dto;

import java.util.UUID;

public record CasePatchRequest(
        Boolean escalationFlag,
        String escalationReason,
        UUID assignedCmId
) {}
