package com.hubaccess.domain.pa;

import com.hubaccess.domain.pa.dto.*;
import com.hubaccess.security.AuthenticatedUser;
import com.hubaccess.web.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases/{caseId}/pa")
@RequiredArgsConstructor
public class PriorAuthorizationController {

    private final PriorAuthorizationService paService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PriorAuthorizationDto>>> listPas(@PathVariable UUID caseId) {
        return ResponseEntity.ok(ApiResponse.ok(paService.listPas(caseId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PriorAuthorizationDto>> createPa(
            @PathVariable UUID caseId,
            @Valid @RequestBody CreatePaRequest request,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(paService.createPa(caseId, request, user)));
    }

    @PatchMapping("/{paId}")
    public ResponseEntity<ApiResponse<PriorAuthorizationDto>> patchPa(
            @PathVariable UUID caseId,
            @PathVariable UUID paId,
            @RequestBody PatchPaRequest request,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(paService.patchPa(caseId, paId, request, user)));
    }
}
