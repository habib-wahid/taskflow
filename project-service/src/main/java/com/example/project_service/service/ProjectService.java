package com.example.project_service.service;

import com.example.project_service.dto.request.AddProjectMemberRequest;
import com.example.project_service.dto.request.CreateProjectRequest;
import com.example.project_service.dto.request.UpdateProjectRequest;
import com.example.project_service.dto.response.ProjectAnalyticsResponse;
import com.example.project_service.dto.response.ProjectMemberResponse;
import com.example.project_service.dto.response.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request, UUID userId);

    List<ProjectResponse> getUserProjects(UUID userId, UUID workspaceId);

    ProjectResponse getProjectById(UUID projectId, UUID userId);

    ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request, UUID userId);

    ProjectResponse archiveProject(UUID projectId, UUID userId);

    ProjectAnalyticsResponse getProjectAnalytics(UUID projectId, UUID userId);

    List<ProjectMemberResponse> getProjectMembers(UUID projectId, UUID userId);

    ProjectMemberResponse addProjectMember(UUID projectId, AddProjectMemberRequest request, UUID userId);

    void removeProjectMember(UUID projectId, UUID memberUserId, UUID userId);
}
