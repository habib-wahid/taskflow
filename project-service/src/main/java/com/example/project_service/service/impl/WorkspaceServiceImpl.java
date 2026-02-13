package com.example.project_service.service.impl;

import com.example.project_service.dto.request.AddWorkspaceMemberRequest;
import com.example.project_service.dto.request.CreateWorkspaceRequest;
import com.example.project_service.dto.request.UpdateWorkspaceRequest;
import com.example.project_service.dto.response.WorkspaceMemberResponse;
import com.example.project_service.dto.response.WorkspaceResponse;
import com.example.project_service.entity.Workspace;
import com.example.project_service.entity.WorkspaceMember;
import com.example.project_service.enums.WorkspaceRole;
import com.example.project_service.exception.AccessDeniedException;
import com.example.project_service.exception.BadRequestException;
import com.example.project_service.exception.DuplicateResourceException;
import com.example.project_service.exception.ResourceNotFoundException;
import com.example.project_service.mapper.WorkspaceMapper;
import com.example.project_service.repository.ProjectRepository;
import com.example.project_service.repository.WorkspaceMemberRepository;
import com.example.project_service.repository.WorkspaceRepository;
import com.example.project_service.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceMapper workspaceMapper;

    @Override
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, UUID userId) {
        log.info("Creating workspace with name: {} for user: {}", request.getName(), userId);

        // Check if slug already exists
        if (workspaceRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Workspace with slug '" + request.getSlug() + "' already exists");
        }

        // Create workspace
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .isActive(true)
                .build();

        workspace = workspaceRepository.save(workspace);

        // Add creator as OWNER
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .userId(userId)
                .role(WorkspaceRole.OWNER)
                .build();

        workspaceMemberRepository.save(ownerMember);

        log.info("Workspace created successfully with id: {}", workspace.getId());
        return workspaceMapper.toResponseWithCounts(workspace, 1, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getUserWorkspaces(UUID userId) {
        log.info("Fetching workspaces for user: {}", userId);
        List<Workspace> workspaces = workspaceRepository.findAllByMemberUserId(userId);
        return workspaceMapper.toResponseList(workspaces);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID workspaceId, UUID userId) {
        log.info("Fetching workspace: {} for user: {}", workspaceId, userId);

        Workspace workspace = getActiveWorkspace(workspaceId);
        validateWorkspaceMembership(workspaceId, userId);

        int memberCount = workspaceMemberRepository.countByWorkspaceId(workspaceId);
        int projectCount = projectRepository.countByWorkspaceId(workspaceId);

        return workspaceMapper.toResponseWithCounts(workspace, memberCount, projectCount);
    }

    @Override
    public WorkspaceResponse updateWorkspace(UUID workspaceId, UpdateWorkspaceRequest request, UUID userId) {
        log.info("Updating workspace: {} by user: {}", workspaceId, userId);

        Workspace workspace = getActiveWorkspace(workspaceId);
        validateWorkspaceAdminAccess(workspaceId, userId);

        // Check slug uniqueness if changed
        if (request.getSlug() != null && !request.getSlug().equals(workspace.getSlug())) {
            if (workspaceRepository.existsBySlug(request.getSlug())) {
                throw new DuplicateResourceException("Workspace with slug '" + request.getSlug() + "' already exists");
            }
            workspace.setSlug(request.getSlug());
        }

        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }

        workspace = workspaceRepository.save(workspace);
        log.info("Workspace updated successfully: {}", workspaceId);

        return workspaceMapper.toResponse(workspace);
    }

    @Override
    public void deleteWorkspace(UUID workspaceId, UUID userId) {
        log.info("Deleting workspace: {} by user: {}", workspaceId, userId);

        Workspace workspace = getActiveWorkspace(workspaceId);
        validateWorkspaceOwnerAccess(workspaceId, userId);

        // Soft delete
        workspace.setIsActive(false);
        workspaceRepository.save(workspace);

        log.info("Workspace deleted (soft) successfully: {}", workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getWorkspaceMembers(UUID workspaceId, UUID userId) {
        log.info("Fetching members for workspace: {} by user: {}", workspaceId, userId);

        getActiveWorkspace(workspaceId);
        validateWorkspaceMembership(workspaceId, userId);

        List<WorkspaceMember> members = workspaceMemberRepository.findAllByWorkspaceId(workspaceId);
        return workspaceMapper.toMemberResponseList(members);
    }

    @Override
    public WorkspaceMemberResponse addWorkspaceMember(UUID workspaceId, AddWorkspaceMemberRequest request, UUID userId) {
        log.info("Adding member: {} to workspace: {} by user: {}", request.getUserId(), workspaceId, userId);

        Workspace workspace = getActiveWorkspace(workspaceId);
        validateWorkspaceAdminAccess(workspaceId, userId);

        // Check if user is already a member
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.getUserId())) {
            throw new DuplicateResourceException("User is already a member of this workspace");
        }

        // Cannot add another OWNER
        if (request.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot add another OWNER to workspace");
        }

        WorkspaceMember newMember = WorkspaceMember.builder()
                .workspace(workspace)
                .userId(request.getUserId())
                .role(request.getRole())
                .build();

        newMember = workspaceMemberRepository.save(newMember);
        log.info("Member added successfully to workspace: {}", workspaceId);

        return workspaceMapper.toMemberResponse(newMember);
    }

    @Override
    public void removeWorkspaceMember(UUID workspaceId, UUID memberUserId, UUID userId) {
        log.info("Removing member: {} from workspace: {} by user: {}", memberUserId, workspaceId, userId);

        getActiveWorkspace(workspaceId);
        validateWorkspaceAdminAccess(workspaceId, userId);

        // Check if member exists
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in workspace"));

        // Cannot remove OWNER
        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot remove workspace OWNER");
        }

        // Cannot remove self
        if (memberUserId.equals(userId)) {
            throw new BadRequestException("Cannot remove yourself from workspace");
        }

        workspaceMemberRepository.delete(member);
        log.info("Member removed successfully from workspace: {}", workspaceId);
    }

    // Helper methods
    private Workspace getActiveWorkspace(UUID workspaceId) {
        return workspaceRepository.findByIdAndIsActiveTrue(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    private void validateWorkspaceMembership(UUID workspaceId, UUID userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("You are not a member of this workspace");
        }
    }

    private void validateWorkspaceAdminAccess(UUID workspaceId, UUID userId) {
        WorkspaceRole role = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));

        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new AccessDeniedException("Only OWNER or ADMIN can perform this action");
        }
    }

    private void validateWorkspaceOwnerAccess(UUID workspaceId, UUID userId) {
        WorkspaceRole role = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));

        if (role != WorkspaceRole.OWNER) {
            throw new AccessDeniedException("Only OWNER can perform this action");
        }
    }
}
