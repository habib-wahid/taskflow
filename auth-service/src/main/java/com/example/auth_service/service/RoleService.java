package com.example.auth_service.service;

import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.AuthException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new AuthException("Role not found: " + name, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Role createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new AuthException("Role already exists: " + name, HttpStatus.CONFLICT);
        }

        Role role = Role.builder()
                .name(name.toUpperCase())
                .description(description)
                .build();

        role = roleRepository.save(role);
        log.info("Role created: {}", name);
        return role;
    }

    @Transactional
    public Role updateRole(UUID roleId, String description) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException("Role not found", HttpStatus.NOT_FOUND));

        role.setDescription(description);
        role = roleRepository.save(role);
        log.info("Role updated: {}", role.getName());
        return role;
    }

    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException("Role not found", HttpStatus.NOT_FOUND));

        // Prevent deletion of default roles
        if (Set.of(Role.SUPER_ADMIN, Role.ADMIN, Role.USER).contains(role.getName())) {
            throw new AuthException("Cannot delete default role: " + role.getName(), HttpStatus.BAD_REQUEST);
        }

        roleRepository.delete(role);
        log.info("Role deleted: {}", role.getName());
    }

    @Transactional
    public User assignRoleToUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AuthException("Role not found: " + roleName, HttpStatus.NOT_FOUND));

        if (user.hasRole(roleName)) {
            throw new AuthException("User already has role: " + roleName, HttpStatus.BAD_REQUEST);
        }

        user.addRole(role);
        user = userRepository.save(user);
        log.info("Role {} assigned to user {}", roleName, maskEmail(user.getEmail()));
        return user;
    }

    @Transactional
    public User removeRoleFromUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AuthException("Role not found: " + roleName, HttpStatus.NOT_FOUND));

        if (!user.hasRole(roleName)) {
            throw new AuthException("User does not have role: " + roleName, HttpStatus.BAD_REQUEST);
        }

        // Prevent removing the last role
        if (user.getRoles().size() <= 1) {
            throw new AuthException("Cannot remove the last role from user", HttpStatus.BAD_REQUEST);
        }

        user.removeRole(role);
        user = userRepository.save(user);
        log.info("Role {} removed from user {}", roleName, maskEmail(user.getEmail()));
        return user;
    }

    @Transactional
    public User setUserRoles(UUID userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Set<Role> newRoles = roleRepository.findByNameIn(roleNames);

        if (newRoles.size() != roleNames.size()) {
            Set<String> foundNames = newRoles.stream().map(Role::getName).collect(Collectors.toSet());
            Set<String> notFound = roleNames.stream()
                    .filter(name -> !foundNames.contains(name))
                    .collect(Collectors.toSet());
            throw new AuthException("Roles not found: " + notFound, HttpStatus.NOT_FOUND);
        }

        if (newRoles.isEmpty()) {
            throw new AuthException("User must have at least one role", HttpStatus.BAD_REQUEST);
        }

        // Clear existing roles and add new ones
        user.getRoles().clear();
        newRoles.forEach(user::addRole);

        user = userRepository.save(user);
        log.info("Roles updated for user {}: {}", maskEmail(user.getEmail()), roleNames);
        return user;
    }

    @Transactional(readOnly = true)
    public Set<String> getUserRoles(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return user.getRoleNames();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
