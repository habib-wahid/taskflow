package com.example.project_service.dto.response;

import com.example.project_service.enums.ProjectRole;
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
public class ProjectMemberResponse {
    private UUID id;
    private UUID userId;
    private ProjectRole role;
    private LocalDateTime joinedAt;
}
