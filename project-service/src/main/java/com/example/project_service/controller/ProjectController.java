package com.example.project_service.controller;

import com.example.project_service.dto.request.AddProjectMemberRequest;
import com.example.project_service.dto.request.CreateProjectRequest;
import com.example.project_service.dto.request.UpdateProjectRequest;
import com.example.project_service.dto.response.ApiResponse;
import com.example.project_service.dto.response.ProjectAnalyticsResponse;
import com.example.project_service.dto.response.ProjectMemberResponse;
import com.example.project_service.dto.response.ProjectResponse;
import com.example.project_service.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Create a new project
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateProjectRequest request) {

        log.info("POST /api/projects - Creating project: {} in workspace: {} by user: {}",
                request.getName(), request.getWorkspaceId(), userId);
        ProjectResponse response = projectService.createProject(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", response));
    }

    /**
     * List user projects (optionally filtered by workspace)
     * GET /api/projects?workspaceId=<uuid>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getUserProjects(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) UUID workspaceId) {

        log.info("GET /api/projects - Fetching projects for user: {} in workspace: {}", userId, workspaceId);
        List<ProjectResponse> projects = projectService.getUserProjects(userId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", projects));
    }

    /**
     * Get project details by ID
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {

        log.info("GET /api/projects/{} - Fetching project for user: {}", id, userId);
        ProjectResponse response = projectService.getProjectById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", response));
    }

    /**
     * Update project
     * PUT /api/projects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {

        log.info("PUT /api/projects/{} - Updating project by user: {}", id, userId);
        ProjectResponse response = projectService.updateProject(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", response));
    }

    /**
     * Archive project
     * PATCH /api/projects/{id}/archive
     */
    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<ProjectResponse>> archiveProject(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {

        log.info("PATCH /api/projects/{}/archive - Archiving project by user: {}", id, userId);
        ProjectResponse response = projectService.archiveProject(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Project archived successfully", response));
    }

    /**
     * Get project analytics
     * GET /api/projects/{id}/analytics
     */
    @GetMapping("/{id}/analytics")
    public ResponseEntity<ApiResponse<ProjectAnalyticsResponse>> getProjectAnalytics(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {

        log.info("GET /api/projects/{}/analytics - Fetching analytics by user: {}", id, userId);
        ProjectAnalyticsResponse response = projectService.getProjectAnalytics(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Project analytics retrieved successfully", response));
    }

    /**
     * List project members
     * GET /api/projects/{id}/members
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {

        log.info("GET /api/projects/{}/members - Fetching members by user: {}", id, userId);
        List<ProjectMemberResponse> members = projectService.getProjectMembers(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Project members retrieved successfully", members));
    }

    /**
     * Add project member
     * POST /api/projects/{id}/members
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody AddProjectMemberRequest request) {

        log.info("POST /api/projects/{}/members - Adding member: {} by user: {}", id, request.getUserId(), userId);
        ProjectMemberResponse response = projectService.addProjectMember(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", response));
    }

    /**
     * Remove project member
     * DELETE /api/projects/{id}/members/{userId}
     */
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeProjectMember(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID memberId) {

        log.info("DELETE /api/projects/{}/members/{} - Removing member by user: {}", id, memberId, userId);
        projectService.removeProjectMember(id, memberId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}
