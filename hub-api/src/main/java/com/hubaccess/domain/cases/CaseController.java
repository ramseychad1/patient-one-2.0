package com.hubaccess.domain.cases;

import com.hubaccess.domain.activity.dto.TimelineEntryDto;
import com.hubaccess.domain.cases.actions.ActionService;
import com.hubaccess.domain.cases.dto.*;
import com.hubaccess.domain.enrollment.EnrollmentService;
import com.hubaccess.domain.enrollment.dto.EnrollmentRequest;
import com.hubaccess.security.AuthenticatedUser;
import com.hubaccess.web.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final ActionService actionService;
    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CaseDetailDto>> createCase(
            @Valid @RequestBody EnrollmentRequest request,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        CaseDetailDto result = enrollmentService.createCase(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CaseListItemDto>>> listCases(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean slaBreachFlag,
            @RequestParam(required = false) String search,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        List<CaseListItemDto> cases = caseService.listCases(user, stage, status, slaBreachFlag, search);
        return ResponseEntity.ok(ApiResponse.ok(cases));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseDetailDto>> getCaseDetail(
            @PathVariable UUID id, Authentication auth) {
        CaseDetailDto detail = caseService.getCaseDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<TimelineEntryDto>>> getTimeline(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "50") int limit,
            Authentication auth) {
        List<TimelineEntryDto> timeline = caseService.getTimeline(id, limit);
        return ResponseEntity.ok(ApiResponse.ok(timeline));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseDetailDto>> patchCase(
            @PathVariable UUID id,
            @RequestBody CasePatchRequest patch,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        CaseDetailDto updated = caseService.patchCase(id, patch, user);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PostMapping("/{id}/actions/{actionKey}")
    public ResponseEntity<ApiResponse<ActionResultDto>> performAction(
            @PathVariable UUID id,
            @PathVariable String actionKey,
            @RequestBody(required = false) Map<String, Object> payload,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        ActionResultDto result = actionService.performAction(id, actionKey, payload, user);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
