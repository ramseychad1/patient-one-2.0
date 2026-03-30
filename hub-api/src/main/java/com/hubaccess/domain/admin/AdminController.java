package com.hubaccess.domain.admin;

import com.hubaccess.domain.auth.*;
import com.hubaccess.domain.manufacturer.Manufacturer;
import com.hubaccess.domain.manufacturer.ManufacturerRepository;
import com.hubaccess.domain.program.Program;
import com.hubaccess.domain.program.ProgramConfig;
import com.hubaccess.domain.program.ProgramConfigRepository;
import com.hubaccess.domain.program.ProgramRepository;
import com.hubaccess.security.AuthenticatedUser;
import com.hubaccess.web.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AdminController {

    private final ManufacturerRepository manufacturerRepo;
    private final ProgramRepository programRepo;
    private final ProgramConfigRepository configRepo;
    private final HubUserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserRoleRepository userRoleRepo;
    private final UserProgramAssignmentRepository assignmentRepo;
    private final PasswordEncoder passwordEncoder;

    // ── Manufacturers ──────────────────────────────────────────────

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listManufacturers() {
        List<Map<String, Object>> result = manufacturerRepo.findAll().stream().map(m -> {
            long programCount = programRepo.findAll().stream()
                    .filter(p -> p.getManufacturerId().equals(m.getId())).count();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("status", m.getStatus());
            map.put("programCount", programCount);
            map.put("createdAt", m.getCreatedAt());
            return map;
        }).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/manufacturers/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getManufacturer(@PathVariable UUID id) {
        Manufacturer m = manufacturerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Manufacturer not found"));
        List<Program> programs = programRepo.findAll().stream()
                .filter(p -> p.getManufacturerId().equals(id)).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", m.getId());
        result.put("name", m.getName());
        result.put("status", m.getStatus());
        result.put("primaryContactName", m.getPrimaryContactName());
        result.put("primaryContactEmail", m.getPrimaryContactEmail());
        result.put("programs", programs);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ── Programs ──────────────────────────────────────────────────

    @GetMapping("/programs")
    public ResponseEntity<ApiResponse<List<Program>>> listPrograms(Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        List<Program> programs;
        if (user.roles().contains("HUB_ADMIN")) {
            programs = programRepo.findAll();
        } else {
            Set<UUID> assignedIds = new HashSet<>();
            assignmentRepo.findByUserId(user.id()).forEach(a -> assignedIds.add(a.getProgramId()));
            programs = programRepo.findAll().stream()
                    .filter(p -> assignedIds.contains(p.getId())).toList();
        }
        return ResponseEntity.ok(ApiResponse.ok(programs));
    }

    @GetMapping("/programs/{id}/config")
    public ResponseEntity<ApiResponse<ProgramConfig>> getProgramConfig(@PathVariable UUID id) {
        ProgramConfig config = configRepo.findByProgramId(id)
                .orElseThrow(() -> new EntityNotFoundException("Program config not found"));
        return ResponseEntity.ok(ApiResponse.ok(config));
    }

    @PutMapping("/programs/{id}/config")
    public ResponseEntity<ApiResponse<ProgramConfig>> updateProgramConfig(
            @PathVariable UUID id,
            @RequestBody ProgramConfig update,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        if (!user.roles().contains("HUB_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("HubAdmin only");
        }
        ProgramConfig config = configRepo.findByProgramId(id)
                .orElseThrow(() -> new EntityNotFoundException("Program config not found"));

        if (update.getPriorAuthRequired() != null) config.setPriorAuthRequired(update.getPriorAuthRequired());
        if (update.getCopayAssistanceEnabled() != null) config.setCopayAssistanceEnabled(update.getCopayAssistanceEnabled());
        if (update.getPapEnabled() != null) config.setPapEnabled(update.getPapEnabled());
        if (update.getBridgeSupplyEnabled() != null) config.setBridgeSupplyEnabled(update.getBridgeSupplyEnabled());
        if (update.getEbvEnabled() != null) config.setEbvEnabled(update.getEbvEnabled());
        if (update.getEivEnabled() != null) config.setEivEnabled(update.getEivEnabled());
        if (update.getFplThresholdPct() != null) config.setFplThresholdPct(update.getFplThresholdPct());
        if (update.getPaSubmitSlaBusinessDays() != null) config.setPaSubmitSlaBusinessDays(update.getPaSubmitSlaBusinessDays());
        if (update.getPaAppealWindowDays() != null) config.setPaAppealWindowDays(update.getPaAppealWindowDays());
        config.setUpdatedAt(OffsetDateTime.now());
        config.setUpdatedBy(user.id());
        configRepo.save(config);
        return ResponseEntity.ok(ApiResponse.ok(config));
    }

    // ── Users ─────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listUsers(Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        if (!user.roles().contains("HUB_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("HubAdmin only");
        }
        List<Map<String, Object>> result = userRepo.findAll().stream().map(u -> {
            List<String> roles = userRoleRepo.findByUserId(u.getId()).stream()
                    .map(ur -> roleRepo.findById(ur.getRoleId()).map(Role::getName).orElse(null))
                    .filter(Objects::nonNull).toList();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("email", u.getEmail());
            map.put("firstName", u.getFirstName());
            map.put("lastName", u.getLastName());
            map.put("isActive", u.getIsActive());
            map.put("isHubAdmin", u.getIsHubAdmin());
            map.put("roles", roles);
            map.put("lastLoginAt", u.getLastLoginAt());
            return map;
        }).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/users/invite")
    public ResponseEntity<ApiResponse<Map<String, Object>>> inviteUser(
            @RequestBody Map<String, String> body, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        if (!user.roles().contains("HUB_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("HubAdmin only");
        }
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        HubUser newUser = HubUser.builder()
                .email(body.get("email"))
                .passwordHash(passwordEncoder.encode(tempPassword))
                .firstName(body.get("firstName"))
                .lastName(body.get("lastName"))
                .isHubAdmin(false)
                .isActive(true)
                .createdBy(user.id())
                .build();
        userRepo.save(newUser);

        String roleName = body.getOrDefault("role", "CASE_MANAGER");
        roleRepo.findByName(roleName).ifPresent(role ->
                userRoleRepo.save(UserRole.builder()
                        .userId(newUser.getId())
                        .roleId(role.getId())
                        .assignedBy(user.id())
                        .build()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", newUser.getId());
        result.put("email", newUser.getEmail());
        result.put("tempPassword", tempPassword);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(
            @PathVariable UUID userId,
            @RequestBody Map<String, Object> body, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        if (!user.roles().contains("HUB_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("HubAdmin only");
        }
        HubUser target = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (body.containsKey("firstName")) target.setFirstName((String) body.get("firstName"));
        if (body.containsKey("lastName")) target.setLastName((String) body.get("lastName"));
        if (body.containsKey("isActive")) target.setIsActive((Boolean) body.get("isActive"));
        if (body.containsKey("password")) {
            String pw = (String) body.get("password");
            if (pw != null && pw.length() >= 6) target.setPasswordHash(passwordEncoder.encode(pw));
        }
        userRepo.save(target);

        // Update role if provided
        if (body.containsKey("role")) {
            String roleName = (String) body.get("role");
            // Remove existing roles
            userRoleRepo.findByUserId(userId).forEach(ur -> userRoleRepo.delete(ur));
            // Add new role
            roleRepo.findByName(roleName).ifPresent(role ->
                    userRoleRepo.save(UserRole.builder()
                            .userId(userId).roleId(role.getId()).assignedBy(user.id()).build()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", target.getId());
        result.put("email", target.getEmail());
        result.put("updated", true);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/users/{userId}/password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePassword(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> body, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        if (!user.roles().contains("HUB_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("HubAdmin only");
        }
        HubUser target = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        target.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(target);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", target.getId());
        result.put("email", target.getEmail());
        result.put("updated", true);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
