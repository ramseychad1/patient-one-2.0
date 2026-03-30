package com.hubaccess.domain.auth.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public record UserInfo(
            UUID id,
            String email,
            String firstName,
            String lastName,
            List<String> roles,
            List<UUID> programs
    ) {}
}
