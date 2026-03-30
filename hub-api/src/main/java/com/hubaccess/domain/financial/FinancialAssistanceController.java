package com.hubaccess.domain.financial;

import com.hubaccess.domain.financial.dto.FaEligibilityResult;
import com.hubaccess.domain.financial.dto.FinancialAssistanceDto;
import com.hubaccess.security.AuthenticatedUser;
import com.hubaccess.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases/{caseId}/financial-assistance")
@RequiredArgsConstructor
public class FinancialAssistanceController {

    private final FinancialAssistanceService faService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FinancialAssistanceDto>>> listFa(@PathVariable UUID caseId) {
        return ResponseEntity.ok(ApiResponse.ok(faService.listFa(caseId)));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<List<FaEligibilityResult>>> evaluate(
            @PathVariable UUID caseId, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(faService.evaluate(caseId, user)));
    }
}
