package com.example.auth_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetUserRolesRequest {

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;
}
