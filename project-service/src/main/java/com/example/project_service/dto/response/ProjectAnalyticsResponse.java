package com.example.project_service.dto.response;

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
public class ProjectAnalyticsResponse {
    private UUID id;
    private UUID projectId;
    private String projectName;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer inProgressTasks;
    private Integer pendingTasks;
    private Integer overdueTasks;
    private Integer totalMembers;
    private Double completionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
