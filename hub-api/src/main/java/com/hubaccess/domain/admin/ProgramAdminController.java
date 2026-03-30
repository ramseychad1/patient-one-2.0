package com.hubaccess.domain.admin;

import com.hubaccess.domain.auth.*;
import com.hubaccess.domain.manufacturer.Manufacturer;
import com.hubaccess.domain.manufacturer.ManufacturerRepository;
import com.hubaccess.domain.program.Program;
import com.hubaccess.domain.program.ProgramConfig;
import com.hubaccess.domain.program.ProgramConfigRepository;
import com.hubaccess.domain.program.ProgramRepository;
import com.hubaccess.security.AuthenticatedUser;
import com.hubaccess.security.JwtService;
import com.hubaccess.web.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProgramAdminController {

    private final ManufacturerRepository manufacturerRepo;
    private final ProgramRepository programRepo;
    private final ProgramConfigRepository configRepo;
    private final HubUserRepository userRepo;
    private final UserProgramAssignmentRepository assignmentRepo;
    private final UserRoleRepository userRoleRepo;
    private final RoleRepository roleRepo;
    private final JwtService jwtService;

    // ── Helpers ───────────────────────────────────────────────────

    private AuthenticatedUser requireAdmin(Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        if (!user.roles().contains("HUB_ADMIN")) {
            throw new AccessDeniedException("HubAdmin only");
        }
        return user;
    }

    // ── Manufacturer CRUD ─────────────────────────────────────────

    @PostMapping("/manufacturers")
    public ResponseEntity<ApiResponse<?>> createManufacturer(
            @RequestBody Map<String, String> body, Authentication auth) {
        AuthenticatedUser user = requireAdmin(auth);

        String name = body.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (manufacturerRepo.findByName(name).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("MANUFACTURER_NAME_EXISTS",
                            "A manufacturer with this name already exists"));
        }

        Manufacturer m = Manufacturer.builder()
                .name(name)
                .primaryContactName(body.get("primaryContactName"))
                .primaryContactEmail(body.get("primaryContactEmail"))
                .primaryContactPhone(body.get("primaryContactPhone"))
                .contractReference(body.get("contractReference"))
                .notes(body.get("notes"))
                .createdBy(user.id())
                .build();
        manufacturerRepo.save(m);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(m));
    }

    @PutMapping("/manufacturers/{id}")
    public ResponseEntity<ApiResponse<Manufacturer>> updateManufacturer(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body, Authentication auth) {
        requireAdmin(auth);

        Manufacturer m = manufacturerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Manufacturer not found"));

        if (body.containsKey("name")) m.setName(body.get("name"));
        if (body.containsKey("primaryContactName")) m.setPrimaryContactName(body.get("primaryContactName"));
        if (body.containsKey("primaryContactEmail")) m.setPrimaryContactEmail(body.get("primaryContactEmail"));
        if (body.containsKey("primaryContactPhone")) m.setPrimaryContactPhone(body.get("primaryContactPhone"));
        if (body.containsKey("contractReference")) m.setContractReference(body.get("contractReference"));
        if (body.containsKey("notes")) m.setNotes(body.get("notes"));
        if (body.containsKey("status")) m.setStatus(body.get("status"));
        manufacturerRepo.save(m);

        return ResponseEntity.ok(ApiResponse.ok(m));
    }

    // ── Program Creation ──────────────────────────────────────────

    @PostMapping("/manufacturers/{manufacturerId}/programs")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createProgram(
            @PathVariable UUID manufacturerId,
            @RequestBody Map<String, Object> body, Authentication auth) {
        AuthenticatedUser user = requireAdmin(auth);

        if (!manufacturerRepo.existsById(manufacturerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "Manufacturer not found"));
        }

        String name = (String) body.get("name");
        String drugBrandName = (String) body.get("drugBrandName");
        if (name == null || name.isBlank() || drugBrandName == null || drugBrandName.isBlank()) {
            throw new IllegalArgumentException("name and drugBrandName are required");
        }

        // Build ndcCodes array from list
        String[] ndcCodes = null;
        Object ndcRaw = body.get("ndcCodes");
        if (ndcRaw instanceof List<?> ndcList) {
            ndcCodes = ndcList.stream().map(Object::toString).toArray(String[]::new);
        }

        // Parse programStartDate
        LocalDate programStartDate = null;
        Object startDateRaw = body.get("programStartDate");
        if (startDateRaw instanceof String s && !s.isBlank()) {
            programStartDate = LocalDate.parse(s);
        }

        Program program = Program.builder()
                .manufacturerId(manufacturerId)
                .name(name)
                .drugBrandName(drugBrandName)
                .drugGenericName((String) body.get("drugGenericName"))
                .ndcCodes(ndcCodes)
                .therapeuticArea((String) body.get("therapeuticArea"))
                .programStartDate(programStartDate)
                .createdBy(user.id())
                .build();
        programRepo.save(program);

        // Create ProgramConfig linked to the new program
        ProgramConfig.ProgramConfigBuilder configBuilder = ProgramConfig.builder()
                .programId(program.getId());

        @SuppressWarnings("unchecked")
        Map<String, Object> cfg = (Map<String, Object>) body.get("config");
        if (cfg != null) {
            if (cfg.get("priorAuthRequired") != null) configBuilder.priorAuthRequired((Boolean) cfg.get("priorAuthRequired"));
            if (cfg.get("copayAssistanceEnabled") != null) configBuilder.copayAssistanceEnabled((Boolean) cfg.get("copayAssistanceEnabled"));
            if (cfg.get("papEnabled") != null) configBuilder.papEnabled((Boolean) cfg.get("papEnabled"));
            if (cfg.get("bridgeSupplyEnabled") != null) configBuilder.bridgeSupplyEnabled((Boolean) cfg.get("bridgeSupplyEnabled"));
            if (cfg.get("quickStartEnabled") != null) configBuilder.quickStartEnabled((Boolean) cfg.get("quickStartEnabled"));
            if (cfg.get("remsTrackingEnabled") != null) configBuilder.remsTrackingEnabled((Boolean) cfg.get("remsTrackingEnabled"));
            if (cfg.get("adherenceProgramEnabled") != null) configBuilder.adherenceProgramEnabled((Boolean) cfg.get("adherenceProgramEnabled"));
            if (cfg.get("ebvEnabled") != null) configBuilder.ebvEnabled((Boolean) cfg.get("ebvEnabled"));
            if (cfg.get("eivEnabled") != null) configBuilder.eivEnabled((Boolean) cfg.get("eivEnabled"));
            if (cfg.get("nurseEducationEnabled") != null) configBuilder.nurseEducationEnabled((Boolean) cfg.get("nurseEducationEnabled"));
            if (cfg.get("welcomeKitEnabled") != null) configBuilder.welcomeKitEnabled((Boolean) cfg.get("welcomeKitEnabled"));
            if (cfg.get("travelAssistanceEnabled") != null) configBuilder.travelAssistanceEnabled((Boolean) cfg.get("travelAssistanceEnabled"));
            if (cfg.get("infusionSiteEnabled") != null) configBuilder.infusionSiteEnabled((Boolean) cfg.get("infusionSiteEnabled"));
            if (cfg.get("fplThresholdPct") != null) configBuilder.fplThresholdPct((Integer) cfg.get("fplThresholdPct"));
            if (cfg.get("paSubmitSlaBusinessDays") != null) configBuilder.paSubmitSlaBusinessDays((Integer) cfg.get("paSubmitSlaBusinessDays"));
            if (cfg.get("paFollowupSlaBusinessDays") != null) configBuilder.paFollowupSlaBusinessDays((Integer) cfg.get("paFollowupSlaBusinessDays"));
            if (cfg.get("paAppealWindowDays") != null) configBuilder.paAppealWindowDays((Integer) cfg.get("paAppealWindowDays"));
            if (cfg.get("paMaxAppealLevels") != null) configBuilder.paMaxAppealLevels((Integer) cfg.get("paMaxAppealLevels"));
            if (cfg.get("paAutoEscalate") != null) configBuilder.paAutoEscalate((Boolean) cfg.get("paAutoEscalate"));
            if (cfg.get("miSlaBusinessDays") != null) configBuilder.miSlaBusinessDays((Integer) cfg.get("miSlaBusinessDays"));
            if (cfg.get("consentMethod") != null) configBuilder.consentMethod((String) cfg.get("consentMethod"));
        }
        configRepo.save(configBuilder.build());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(program));
    }

    // ── User-Program Assignments ──────────────────────────────────

    @PostMapping("/programs/{programId}/users")
    public ResponseEntity<ApiResponse<?>> assignUserToProgram(
            @PathVariable UUID programId,
            @RequestBody Map<String, Object> body, Authentication auth) {
        AuthenticatedUser admin = requireAdmin(auth);

        UUID userId = UUID.fromString((String) body.get("userId"));

        // Check for existing assignment (UNIQUE constraint)
        if (assignmentRepo.findByUserIdAndProgramId(userId, programId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("DUPLICATE_ASSIGNMENT",
                            "User is already assigned to this program"));
        }

        UserProgramAssignment assignment = UserProgramAssignment.builder()
                .userId(userId)
                .programId(programId)
                .canCreateCases(body.get("canCreateCases") != null ? (Boolean) body.get("canCreateCases") : true)
                .canEditCases(body.get("canEditCases") != null ? (Boolean) body.get("canEditCases") : true)
                .canCloseCases(body.get("canCloseCases") != null ? (Boolean) body.get("canCloseCases") : false)
                .canViewFinancials(body.get("canViewFinancials") != null ? (Boolean) body.get("canViewFinancials") : true)
                .canPerformActions(body.get("canPerformActions") != null ? (Boolean) body.get("canPerformActions") : true)
                .expiresAt(body.get("expiresAt") != null ? OffsetDateTime.parse((String) body.get("expiresAt")) : null)
                .assignedBy(admin.id())
                .build();
        assignmentRepo.save(assignment);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(assignment));
    }

    @PutMapping("/programs/{programId}/users/{userId}")
    public ResponseEntity<ApiResponse<UserProgramAssignment>> updateUserAssignment(
            @PathVariable UUID programId,
            @PathVariable UUID userId,
            @RequestBody Map<String, Object> body, Authentication auth) {
        requireAdmin(auth);

        UserProgramAssignment assignment = assignmentRepo.findByUserIdAndProgramId(userId, programId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (body.containsKey("canCreateCases")) assignment.setCanCreateCases((Boolean) body.get("canCreateCases"));
        if (body.containsKey("canEditCases")) assignment.setCanEditCases((Boolean) body.get("canEditCases"));
        if (body.containsKey("canCloseCases")) assignment.setCanCloseCases((Boolean) body.get("canCloseCases"));
        if (body.containsKey("canViewFinancials")) assignment.setCanViewFinancials((Boolean) body.get("canViewFinancials"));
        if (body.containsKey("canPerformActions")) assignment.setCanPerformActions((Boolean) body.get("canPerformActions"));
        if (body.containsKey("expiresAt")) {
            String val = (String) body.get("expiresAt");
            assignment.setExpiresAt(val != null ? OffsetDateTime.parse(val) : null);
        }
        assignmentRepo.save(assignment);

        return ResponseEntity.ok(ApiResponse.ok(assignment));
    }

    @DeleteMapping("/programs/{programId}/users/{userId}")
    public ResponseEntity<Void> removeUserAssignment(
            @PathVariable UUID programId,
            @PathVariable UUID userId, Authentication auth) {
        requireAdmin(auth);

        assignmentRepo.findByUserIdAndProgramId(userId, programId)
                .ifPresent(assignmentRepo::delete);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/programs/{programId}/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listProgramUsers(
            @PathVariable UUID programId, Authentication auth) {
        requireAdmin(auth);

        List<Map<String, Object>> result = assignmentRepo.findByProgramId(programId).stream().map(a -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("assignmentId", a.getId());
            map.put("userId", a.getUserId());
            map.put("programId", a.getProgramId());
            map.put("canCreateCases", a.getCanCreateCases());
            map.put("canEditCases", a.getCanEditCases());
            map.put("canCloseCases", a.getCanCloseCases());
            map.put("canViewFinancials", a.getCanViewFinancials());
            map.put("canPerformActions", a.getCanPerformActions());
            map.put("expiresAt", a.getExpiresAt());
            map.put("assignedAt", a.getAssignedAt());

            // Enrich with user details
            userRepo.findById(a.getUserId()).ifPresent(u -> {
                map.put("firstName", u.getFirstName());
                map.put("lastName", u.getLastName());
                map.put("email", u.getEmail());
            });

            // Enrich with roles
            List<String> roles = userRoleRepo.findByUserId(a.getUserId()).stream()
                    .map(ur -> roleRepo.findById(ur.getRoleId()).map(Role::getName).orElse(null))
                    .filter(Objects::nonNull).toList();
            map.put("roles", roles);

            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ── My Programs & Active Program ──────────────────────────────

    @GetMapping("/users/me/programs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myPrograms(Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

        List<Program> programs;
        if (user.roles().contains("HUB_ADMIN")) {
            programs = programRepo.findAll().stream()
                    .filter(p -> "ACTIVE".equals(p.getStatus())).toList();
        } else {
            Set<UUID> assignedIds = new HashSet<>();
            assignmentRepo.findByUserId(user.id()).forEach(a -> assignedIds.add(a.getProgramId()));
            programs = programRepo.findAll().stream()
                    .filter(p -> assignedIds.contains(p.getId())).toList();
        }

        // Build response with manufacturer name
        List<Map<String, Object>> result = programs.stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("drugBrandName", p.getDrugBrandName());
            map.put("drugGenericName", p.getDrugGenericName());
            map.put("therapeuticArea", p.getTherapeuticArea());
            map.put("status", p.getStatus());
            map.put("programStartDate", p.getProgramStartDate());

            manufacturerRepo.findById(p.getManufacturerId()).ifPresent(m -> {
                map.put("manufacturerId", m.getId());
                map.put("manufacturerName", m.getName());
            });

            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/users/me/active-program")
    public ResponseEntity<ApiResponse<?>> setActiveProgram(
            @RequestBody Map<String, String> body, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

        UUID programId = UUID.fromString(body.get("programId"));

        // Verify user has access: admin or assigned
        boolean hasAccess = user.roles().contains("HUB_ADMIN")
                || assignmentRepo.findByUserIdAndProgramId(user.id(), programId).isPresent();
        if (!hasAccess) {
            throw new AccessDeniedException("No access to this program");
        }

        Program program = programRepo.findById(programId)
                .orElseThrow(() -> new EntityNotFoundException("Program not found"));

        // Gather program IDs for JWT
        List<UUID> programIds;
        if (user.roles().contains("HUB_ADMIN")) {
            programIds = programRepo.findAll().stream().map(Program::getId).toList();
        } else {
            programIds = assignmentRepo.findByUserId(user.id()).stream()
                    .map(UserProgramAssignment::getProgramId).toList();
        }

        String accessToken = jwtService.generateAccessTokenWithActiveProgram(
                user.id(), user.email(), user.roles(), programIds, programId);

        Map<String, Object> activeProgram = new LinkedHashMap<>();
        activeProgram.put("id", program.getId());
        activeProgram.put("name", program.getName());
        activeProgram.put("drugBrandName", program.getDrugBrandName());
        manufacturerRepo.findById(program.getManufacturerId())
                .ifPresent(m -> activeProgram.put("manufacturerName", m.getName()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accessToken", accessToken);
        result.put("activeProgram", activeProgram);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
