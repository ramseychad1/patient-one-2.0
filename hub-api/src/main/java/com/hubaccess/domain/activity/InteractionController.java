package com.hubaccess.domain.activity;

import com.hubaccess.domain.activity.dto.CreateInteractionRequest;
import com.hubaccess.domain.activity.dto.InteractionDto;
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
@RequestMapping("/api/v1/cases/{caseId}/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InteractionDto>>> listInteractions(
            @PathVariable UUID caseId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(interactionService.listInteractions(caseId, limit)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InteractionDto>> createInteraction(
            @PathVariable UUID caseId,
            @Valid @RequestBody CreateInteractionRequest request,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(interactionService.createInteraction(caseId, request, user)));
    }
}
