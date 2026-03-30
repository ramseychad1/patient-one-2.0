package com.hubaccess.domain.cases;

import com.hubaccess.domain.cases.dto.DashboardDto;
import com.hubaccess.security.AuthenticatedUser;
import com.hubaccess.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final CaseService caseService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard(Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        DashboardDto dashboard = caseService.getDashboard(user);
        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }
}
