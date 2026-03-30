package com.hubaccess.domain.activity;

import com.hubaccess.domain.activity.dto.*;
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
@RequiredArgsConstructor
public class CaseTaskController {

    private final CaseTaskService taskService;

    @GetMapping("/api/v1/cases/{caseId}/tasks")
    public ResponseEntity<ApiResponse<List<CaseTaskDto>>> listTasks(@PathVariable UUID caseId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.listTasks(caseId)));
    }

    @PostMapping("/api/v1/cases/{caseId}/tasks")
    public ResponseEntity<ApiResponse<CaseTaskDto>> createTask(
            @PathVariable UUID caseId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(taskService.createTask(caseId, request, user)));
    }

    @PatchMapping("/api/v1/cases/{caseId}/tasks/{taskId}")
    public ResponseEntity<ApiResponse<CaseTaskDto>> patchTask(
            @PathVariable UUID caseId,
            @PathVariable UUID taskId,
            @RequestBody PatchTaskRequest request,
            Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(taskService.patchTask(caseId, taskId, request, user)));
    }

    @GetMapping("/api/v1/tasks/mine")
    public ResponseEntity<ApiResponse<List<CaseTaskDto>>> getMyTasks(Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(taskService.getMyTasks(user)));
    }
}
