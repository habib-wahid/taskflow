package com.example.project_service.dto.response;

import com.example.project_service.enums.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {
    private UUID id;
    private UUID userId;
    private WorkspaceRole role;
    private LocalDateTime joinedAt;
}
