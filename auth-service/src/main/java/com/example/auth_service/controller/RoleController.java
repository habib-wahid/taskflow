package com.example.auth_service.controller;

import com.example.auth_service.dto.request.AssignRoleRequest;
import com.example.auth_service.dto.request.CreateRoleRequest;
import com.example.auth_service.dto.request.SetUserRolesRequest;
import com.example.auth_service.dto.request.UpdateRoleRequest;
import com.example.auth_service.dto.response.MessageResponse;
import com.example.auth_service.dto.response.RoleResponse;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * GET /roles
     * Get all roles (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        log.debug("Get all roles request received");
        List<RoleResponse> roles = roleService.getAllRoles().stream()
                .map(RoleResponse::fromRole)
                .toList();
        return ResponseEntity.ok(roles);
    }

    /**
     * GET /roles/{name}
     * Get role by name (Admin only)
     */
    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RoleResponse> getRoleByName(@PathVariable String name) {
        log.debug("Get role request received: {}", name);
        Role role = roleService.getRoleByName(name);
        return ResponseEntity.ok(RoleResponse.fromRole(role));
    }

    /**
     * POST /roles
     * Create a new role (Super Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.debug("Create role request received: {}", request.getName());
        Role role = roleService.createRole(request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.fromRole(role));
    }

    /**
     * PUT /roles/{roleId}
     * Update a role (Super Admin only)
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.debug("Update role request received: {}", roleId);
        Role role = roleService.updateRole(roleId, request.getDescription());
        return ResponseEntity.ok(RoleResponse.fromRole(role));
    }

    /**
     * DELETE /roles/{roleId}
     * Delete a role (Super Admin only)
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MessageResponse> deleteRole(@PathVariable UUID roleId) {
        log.debug("Delete role request received: {}", roleId);
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(MessageResponse.of("Role deleted successfully"));
    }

    /**
     * POST /roles/users/{userId}/assign
     * Assign a role to a user (Admin only)
     */
    @PostMapping("/users/{userId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> assignRoleToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request) {
        log.debug("Assign role {} to user {} request received", request.getRoleName(), userId);
        User user = roleService.assignRoleToUser(userId, request.getRoleName());
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    /**
     * POST /roles/users/{userId}/remove
     * Remove a role from a user (Admin only)
     */
    @PostMapping("/users/{userId}/remove")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> removeRoleFromUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request) {
        log.debug("Remove role {} from user {} request received", request.getRoleName(), userId);
        User user = roleService.removeRoleFromUser(userId, request.getRoleName());
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    /**
     * PUT /roles/users/{userId}
     * Set all roles for a user (Admin only)
     */
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> setUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody SetUserRolesRequest request) {
        log.debug("Set roles {} for user {} request received", request.getRoles(), userId);
        User user = roleService.setUserRoles(userId, request.getRoles());
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    /**
     * GET /roles/users/{userId}
     * Get all roles for a user (Admin only)
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable UUID userId) {
        log.debug("Get roles for user {} request received", userId);
        Set<String> roles = roleService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }
}
