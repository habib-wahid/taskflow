package com.example.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean emailVerified;
    private Set<String> roles;
    private String message;
}
