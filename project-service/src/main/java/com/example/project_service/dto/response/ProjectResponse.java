package com.example.project_service.dto.response;

import com.example.project_service.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private UUID workspaceId;
    private String workspaceName;
    private String name;
    private String description;
    private String key;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer memberCount;
}
