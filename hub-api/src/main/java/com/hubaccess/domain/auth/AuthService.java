package com.hubaccess.domain.auth;

import com.hubaccess.domain.auth.dto.LoginRequest;
import com.hubaccess.domain.auth.dto.LoginResponse;
import com.hubaccess.domain.auth.dto.TokenResponse;
import com.hubaccess.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final HubUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserProgramAssignmentRepository programAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        HubUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!user.getIsActive()) {
            throw new AuthException("Account is inactive");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }

        // Update last login
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        List<String> roles = getUserRoles(user.getId());
        List<UUID> programIds = getUserProgramIds(user.getId());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles, programIds);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        roles,
                        programIds
                )
        );
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new AuthException("Invalid refresh token");
        }

        UUID userId = jwtService.getUserId(refreshToken);
        HubUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        if (!user.getIsActive()) {
            throw new AuthException("Account is inactive");
        }

        List<String> roles = getUserRoles(user.getId());
        List<UUID> programIds = getUserProgramIds(user.getId());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles, programIds);
        return new TokenResponse(accessToken);
    }

    // TODO: Implement refresh token revocation table for logout
    public void logout(String refreshToken) {
        // For now, client-side token removal is sufficient
        // In production: store revoked refresh tokens in a table and check on refresh
    }

    private List<String> getUserRoles(UUID userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId())
                        .map(Role::getName)
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private List<UUID> getUserProgramIds(UUID userId) {
        return programAssignmentRepository.findByUserId(userId).stream()
                .filter(upa -> upa.getExpiresAt() == null || upa.getExpiresAt().isAfter(OffsetDateTime.now()))
                .map(UserProgramAssignment::getProgramId)
                .toList();
    }

    public static class AuthException extends RuntimeException {
        public AuthException(String message) {
            super(message);
        }
    }
}
