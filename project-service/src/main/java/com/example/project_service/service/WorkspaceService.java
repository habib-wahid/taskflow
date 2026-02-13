package com.example.project_service.service;

import com.example.project_service.dto.request.AddWorkspaceMemberRequest;
import com.example.project_service.dto.request.CreateWorkspaceRequest;
import com.example.project_service.dto.request.UpdateWorkspaceRequest;
import com.example.project_service.dto.response.WorkspaceMemberResponse;
import com.example.project_service.dto.response.WorkspaceResponse;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {

    WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, UUID userId);

    List<WorkspaceResponse> getUserWorkspaces(UUID userId);

    WorkspaceResponse getWorkspaceById(UUID workspaceId, UUID userId);

    WorkspaceResponse updateWorkspace(UUID workspaceId, UpdateWorkspaceRequest request, UUID userId);

    void deleteWorkspace(UUID workspaceId, UUID userId);

    List<WorkspaceMemberResponse> getWorkspaceMembers(UUID workspaceId, UUID userId);

    WorkspaceMemberResponse addWorkspaceMember(UUID workspaceId, AddWorkspaceMemberRequest request, UUID userId);

    void removeWorkspaceMember(UUID workspaceId, UUID memberUserId, UUID userId);
}
