package com.hubaccess.domain.outreach;

import com.hubaccess.domain.outreach.dto.CreateOutreachRequest;
import com.hubaccess.domain.outreach.dto.PatientOutreachDto;
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
@RequestMapping("/api/v1/cases/{caseId}/outreach")
@RequiredArgsConstructor
public class OutreachController {

    private final OutreachService outreachService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientOutreachDto>>> listOutreach(@PathVariable UUID caseId) {
        return ResponseEntity.ok(ApiResponse.ok(outreachService.listOutreach(caseId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientOutreachDto>> createOutreach(
            @PathVariable UUID caseId,
            @Valid @RequestBody CreateOutreachRequest request,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(outreachService.createOutreach(caseId, request, user)));
    }
}
