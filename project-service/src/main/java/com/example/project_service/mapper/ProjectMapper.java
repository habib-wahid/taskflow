package com.example.project_service.mapper;

import com.example.project_service.dto.response.ProjectAnalyticsResponse;
import com.example.project_service.dto.response.ProjectMemberResponse;
import com.example.project_service.dto.response.ProjectResponse;
import com.example.project_service.entity.Project;
import com.example.project_service.entity.ProjectAnalytics;
import com.example.project_service.entity.ProjectMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .workspaceId(project.getWorkspace().getId())
                .workspaceName(project.getWorkspace().getName())
                .name(project.getName())
                .description(project.getDescription())
                .key(project.getKey())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .budget(project.getBudget())
                .isActive(project.getIsActive())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .memberCount(project.getMembers() != null ? project.getMembers().size() : 0)
                .build();
    }

    public ProjectResponse toResponseWithMemberCount(Project project, int memberCount) {
        return ProjectResponse.builder()
                .id(project.getId())
                .workspaceId(project.getWorkspace().getId())
                .workspaceName(project.getWorkspace().getName())
                .name(project.getName())
                .description(project.getDescription())
                .key(project.getKey())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .budget(project.getBudget())
                .isActive(project.getIsActive())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .memberCount(memberCount)
                .build();
    }

    public List<ProjectResponse> toResponseList(List<Project> projects) {
        return projects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    public List<ProjectMemberResponse> toMemberResponseList(List<ProjectMember> members) {
        return members.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    public ProjectAnalyticsResponse toAnalyticsResponse(ProjectAnalytics analytics) {
        double completionPercentage = 0.0;
        if (analytics.getTotalTasks() > 0) {
            completionPercentage = (double) analytics.getCompletedTasks() / analytics.getTotalTasks() * 100;
        }

        return ProjectAnalyticsResponse.builder()
                .id(analytics.getId())
                .projectId(analytics.getProject().getId())
                .projectName(analytics.getProject().getName())
                .totalTasks(analytics.getTotalTasks())
                .completedTasks(analytics.getCompletedTasks())
                .inProgressTasks(analytics.getInProgressTasks())
                .pendingTasks(analytics.getPendingTasks())
                .overdueTasks(analytics.getOverdueTasks())
                .totalMembers(analytics.getTotalMembers())
                .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
                .createdAt(analytics.getCreatedAt())
                .updatedAt(analytics.getUpdatedAt())
                .build();
    }
}
