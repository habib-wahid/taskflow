package com.example.project_service.controller;

import com.example.project_service.dto.request.AddWorkspaceMemberRequest;
import com.example.project_service.dto.request.CreateWorkspaceRequest;
import com.example.project_service.dto.request.UpdateWorkspaceRequest;
import com.example.project_service.dto.response.ApiResponse;
import com.example.project_service.dto.response.WorkspaceMemberResponse;
import com.example.project_service.dto.response.WorkspaceResponse;
import com.example.project_service.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    /**
     * Create a new workspace
     * POST /api/workspaces
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateWorkspaceRequest request) {

        log.info("POST /api/workspaces - Creating workspace: {} by user: {}", request.getName(), userId);
        WorkspaceResponse response = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created successfully", response));
    }

    /**
     * List all workspaces for the current user
     * GET /api/workspaces
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getUserWorkspaces(
            @RequestHeader("X-User-Id") UUID userId) {

        log.info("GET /api/workspaces - Fetching workspaces for user: {}", userId);
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(userId);
        return ResponseEntity.ok(ApiResponse.success("Workspaces retrieved successfully", workspaces));
    }

    /**
     * Get workspace details by ID
     * GET /api/workspaces/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceById(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {

        log.info("GET /api/workspaces/{} - Fetching workspace for user: {}", id, userId);
        WorkspaceResponse response = workspaceService.getWorkspaceById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace retrieved successfully", response));
    }

    /**
     * Update workspace
     * PUT /api/workspaces/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request) {

        log.info("PUT /api/workspaces/{} - Updating workspace by user: {}", id, userId);
        WorkspaceResponse response = workspaceService.updateWorkspace(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated successfully", response));
    }

    /**
     * Delete workspace (soft delete)
     * DELETE /api/workspaces/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {

        log.info("DELETE /api/workspaces/{} - Deleting workspace by user: {}", id, userId);
        workspaceService.deleteWorkspace(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted successfully", null));
    }

    /**
     * List workspace members
     * GET /api/workspaces/{id}/members
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> getWorkspaceMembers(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {

        log.info("GET /api/workspaces/{}/members - Fetching members by user: {}", id, userId);
        List<WorkspaceMemberResponse> members = workspaceService.getWorkspaceMembers(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace members retrieved successfully", members));
    }

    /**
     * Add workspace member
     * POST /api/workspaces/{id}/members
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> addWorkspaceMember(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody AddWorkspaceMemberRequest request) {

        log.info("POST /api/workspaces/{}/members - Adding member: {} by user: {}", id, request.getUserId(), userId);
        WorkspaceMemberResponse response = workspaceService.addWorkspaceMember(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", response));
    }

    /**
     * Remove workspace member
     * DELETE /api/workspaces/{id}/members/{userId}
     */
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeWorkspaceMember(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID memberId) {

        log.info("DELETE /api/workspaces/{}/members/{} - Removing member by user: {}", id, memberId, userId);
        workspaceService.removeWorkspaceMember(id, memberId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}
