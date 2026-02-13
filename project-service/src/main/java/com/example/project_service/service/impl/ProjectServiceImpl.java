package com.example.project_service.service.impl;

import com.example.project_service.dto.request.AddProjectMemberRequest;
import com.example.project_service.dto.request.CreateProjectRequest;
import com.example.project_service.dto.request.UpdateProjectRequest;
import com.example.project_service.dto.response.ProjectAnalyticsResponse;
import com.example.project_service.dto.response.ProjectMemberResponse;
import com.example.project_service.dto.response.ProjectResponse;
import com.example.project_service.entity.Project;
import com.example.project_service.entity.ProjectAnalytics;
import com.example.project_service.entity.ProjectMember;
import com.example.project_service.entity.Workspace;
import com.example.project_service.enums.ProjectRole;
import com.example.project_service.enums.ProjectStatus;
import com.example.project_service.enums.WorkspaceRole;
import com.example.project_service.exception.AccessDeniedException;
import com.example.project_service.exception.BadRequestException;
import com.example.project_service.exception.DuplicateResourceException;
import com.example.project_service.exception.ResourceNotFoundException;
import com.example.project_service.mapper.ProjectMapper;
import com.example.project_service.repository.*;
import com.example.project_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectAnalyticsRepository projectAnalyticsRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request, UUID userId) {
        log.info("Creating project with name: {} in workspace: {} by user: {}",
                request.getName(), request.getWorkspaceId(), userId);

        // Validate workspace exists and user is a member
        Workspace workspace = workspaceRepository.findByIdAndIsActiveTrue(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        // Validate user has at least MEMBER role in workspace
        WorkspaceRole workspaceRole = workspaceMemberRepository
                .findRoleByWorkspaceIdAndUserId(request.getWorkspaceId(), userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));

        if (workspaceRole == WorkspaceRole.VIEWER) {
            throw new AccessDeniedException("VIEWER cannot create projects");
        }

        // Check if project key already exists in workspace
        if (projectRepository.existsByWorkspaceIdAndKey(request.getWorkspaceId(), request.getKey())) {
            throw new DuplicateResourceException("Project with key '" + request.getKey() + "' already exists in this workspace");
        }

        // Create project
        Project project = Project.builder()
                .workspace(workspace)
                .name(request.getName())
                .description(request.getDescription())
                .key(request.getKey().toUpperCase())
                .status(ProjectStatus.ACTIVE)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .isActive(true)
                .build();

        project = projectRepository.save(project);

        // Add creator as OWNER
        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .userId(userId)
                .role(ProjectRole.OWNER)
                .build();

        projectMemberRepository.save(ownerMember);

        // Create initial analytics
        ProjectAnalytics analytics = ProjectAnalytics.builder()
                .project(project)
                .totalTasks(0)
                .completedTasks(0)
                .inProgressTasks(0)
                .pendingTasks(0)
                .overdueTasks(0)
                .totalMembers(1)
                .build();

        projectAnalyticsRepository.save(analytics);

        log.info("Project created successfully with id: {}", project.getId());
        return projectMapper.toResponseWithMemberCount(project, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(UUID userId, UUID workspaceId) {
        log.info("Fetching projects for user: {} in workspace: {}", userId, workspaceId);

        List<Project> projects;
        if (workspaceId != null) {
            // Validate user is member of workspace
            if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
                throw new AccessDeniedException("You are not a member of this workspace");
            }
            projects = projectRepository.findAllByWorkspaceIdAndMemberUserId(workspaceId, userId);
        } else {
            projects = projectRepository.findAllByMemberUserId(userId);
        }

        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId, UUID userId) {
        log.info("Fetching project: {} for user: {}", projectId, userId);

        Project project = getActiveProject(projectId);
        validateProjectAccess(projectId, project.getWorkspace().getId(), userId);

        int memberCount = projectMemberRepository.countByProjectId(projectId);
        return projectMapper.toResponseWithMemberCount(project, memberCount);
    }

    @Override
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request, UUID userId) {
        log.info("Updating project: {} by user: {}", projectId, userId);

        Project project = getActiveProject(projectId);
        validateProjectAdminAccess(projectId, project.getWorkspace().getId(), userId);

        // Update fields if provided
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getKey() != null && !request.getKey().equals(project.getKey())) {
            // Check if new key already exists
            if (projectRepository.existsByWorkspaceIdAndKey(project.getWorkspace().getId(), request.getKey())) {
                throw new DuplicateResourceException("Project with key '" + request.getKey() + "' already exists in this workspace");
            }
            project.setKey(request.getKey().toUpperCase());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getBudget() != null) {
            project.setBudget(request.getBudget());
        }

        project = projectRepository.save(project);
        log.info("Project updated successfully: {}", projectId);

        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse archiveProject(UUID projectId, UUID userId) {
        log.info("Archiving project: {} by user: {}", projectId, userId);

        Project project = getActiveProject(projectId);
        validateProjectAdminAccess(projectId, project.getWorkspace().getId(), userId);

        project.setStatus(ProjectStatus.ARCHIVED);
        project = projectRepository.save(project);

        log.info("Project archived successfully: {}", projectId);
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectAnalyticsResponse getProjectAnalytics(UUID projectId, UUID userId) {
        log.info("Fetching analytics for project: {} by user: {}", projectId, userId);

        Project project = getActiveProject(projectId);
        validateProjectAccess(projectId, project.getWorkspace().getId(), userId);

        ProjectAnalytics analytics = projectAnalyticsRepository.findByProjectId(projectId)
                .orElseGet(() -> {
                    // Create analytics if not exists
                    int memberCount = projectMemberRepository.countByProjectId(projectId);
                    ProjectAnalytics newAnalytics = ProjectAnalytics.builder()
                            .project(project)
                            .totalTasks(0)
                            .completedTasks(0)
                            .inProgressTasks(0)
                            .pendingTasks(0)
                            .overdueTasks(0)
                            .totalMembers(memberCount)
                            .build();
                    return projectAnalyticsRepository.save(newAnalytics);
                });

        return projectMapper.toAnalyticsResponse(analytics);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(UUID projectId, UUID userId) {
        log.info("Fetching members for project: {} by user: {}", projectId, userId);

        Project project = getActiveProject(projectId);
        validateProjectAccess(projectId, project.getWorkspace().getId(), userId);

        List<ProjectMember> members = projectMemberRepository.findAllByProjectId(projectId);
        return projectMapper.toMemberResponseList(members);
    }

    @Override
    public ProjectMemberResponse addProjectMember(UUID projectId, AddProjectMemberRequest request, UUID userId) {
        log.info("Adding member: {} to project: {} by user: {}", request.getUserId(), projectId, userId);

        Project project = getActiveProject(projectId);
        validateProjectAdminAccess(projectId, project.getWorkspace().getId(), userId);

        // Check if user is already a member
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new DuplicateResourceException("User is already a member of this project");
        }

        // Validate user is a workspace member
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(project.getWorkspace().getId(), request.getUserId())) {
            throw new BadRequestException("User must be a workspace member first");
        }

        // Cannot add another OWNER
        if (request.getRole() == ProjectRole.OWNER) {
            throw new BadRequestException("Cannot add another OWNER to project");
        }

        ProjectMember newMember = ProjectMember.builder()
                .project(project)
                .userId(request.getUserId())
                .role(request.getRole())
                .build();

        newMember = projectMemberRepository.save(newMember);

        // Update analytics
        projectAnalyticsRepository.findByProjectId(projectId).ifPresent(analytics -> {
            analytics.setTotalMembers(analytics.getTotalMembers() + 1);
            projectAnalyticsRepository.save(analytics);
        });

        log.info("Member added successfully to project: {}", projectId);
        return projectMapper.toMemberResponse(newMember);
    }

    @Override
    public void removeProjectMember(UUID projectId, UUID memberUserId, UUID userId) {
        log.info("Removing member: {} from project: {} by user: {}", memberUserId, projectId, userId);

        Project project = getActiveProject(projectId);
        validateProjectAdminAccess(projectId, project.getWorkspace().getId(), userId);

        // Check if member exists
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in project"));

        // Cannot remove OWNER
        if (member.getRole() == ProjectRole.OWNER) {
            throw new BadRequestException("Cannot remove project OWNER");
        }

        // Cannot remove self
        if (memberUserId.equals(userId)) {
            throw new BadRequestException("Cannot remove yourself from project");
        }

        projectMemberRepository.delete(member);

        // Update analytics
        projectAnalyticsRepository.findByProjectId(projectId).ifPresent(analytics -> {
            analytics.setTotalMembers(Math.max(0, analytics.getTotalMembers() - 1));
            projectAnalyticsRepository.save(analytics);
        });

        log.info("Member removed successfully from project: {}", projectId);
    }

    // Helper methods
    private Project getActiveProject(UUID projectId) {
        return projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void validateProjectAccess(UUID projectId, UUID workspaceId, UUID userId) {
        // Check project membership first
        Optional<ProjectRole> projectRole = projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId);
        if (projectRole.isPresent()) {
            return; // User has project-level access
        }

        // Fall back to workspace membership
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
    }

    private void validateProjectAdminAccess(UUID projectId, UUID workspaceId, UUID userId) {
        // Check project-level role first (project role overrides workspace role)
        Optional<ProjectRole> projectRole = projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId);
        if (projectRole.isPresent()) {
            if (projectRole.get() == ProjectRole.OWNER || projectRole.get() == ProjectRole.ADMIN) {
                return;
            }
            throw new AccessDeniedException("Only project OWNER or ADMIN can perform this action");
        }

        // Fall back to workspace role
        WorkspaceRole workspaceRole = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have access to this project"));

        if (workspaceRole != WorkspaceRole.OWNER && workspaceRole != WorkspaceRole.ADMIN) {
            throw new AccessDeniedException("Only OWNER or ADMIN can perform this action");
        }
    }
}
