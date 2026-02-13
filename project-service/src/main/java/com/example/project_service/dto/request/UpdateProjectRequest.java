package com.example.project_service.dto.request;

import com.example.project_service.enums.ProjectStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(min = 2, max = 10, message = "Key must be between 2 and 10 characters")
    private String key;

    private ProjectStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal budget;
}
