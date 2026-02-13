package com.example.project_service.mapper;

import com.example.project_service.dto.response.WorkspaceMemberResponse;
import com.example.project_service.dto.response.WorkspaceResponse;
import com.example.project_service.entity.Workspace;
import com.example.project_service.entity.WorkspaceMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkspaceMapper {

    public WorkspaceResponse toResponse(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .slug(workspace.getSlug())
                .isActive(workspace.getIsActive())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .memberCount(workspace.getMembers() != null ? workspace.getMembers().size() : 0)
                .projectCount(workspace.getProjects() != null ?
                        (int) workspace.getProjects().stream().filter(p -> p.getIsActive()).count() : 0)
                .build();
    }

    public WorkspaceResponse toResponseWithCounts(Workspace workspace, int memberCount, int projectCount) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .slug(workspace.getSlug())
                .isActive(workspace.getIsActive())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .memberCount(memberCount)
                .projectCount(projectCount)
                .build();
    }

    public List<WorkspaceResponse> toResponseList(List<Workspace> workspaces) {
        return workspaces.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public WorkspaceMemberResponse toMemberResponse(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    public List<WorkspaceMemberResponse> toMemberResponseList(List<WorkspaceMember> members) {
        return members.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }
}
