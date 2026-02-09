package com.example.auth_service.dto.response;

import com.example.auth_service.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private Boolean emailVerified;
    private Boolean isActive;
    private Set<String> roles;
    private Map<String, Object> preferences;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .roles(user.getRoleNames())
                .preferences(user.getPreferences())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public static UserResponse fromUserBasic(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoleNames())
                .avatarUrl(user.getAvatarUrl())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
