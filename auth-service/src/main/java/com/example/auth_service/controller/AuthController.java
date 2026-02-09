package com.example.auth_service.controller;

import com.example.auth_service.dto.request.*;
import com.example.auth_service.dto.response.*;
import com.example.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /register
     * Register a new user account
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Registration request received for email: {}", maskEmail(request.getEmail()));
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /login
     * Authenticate user and issue JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request received for email: {}", maskEmail(request.getEmail()));
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /refresh
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request received");
        TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /logout
     * Logout user and revoke refresh token
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody(required = false) LogoutRequest request) {
        log.debug("Logout request received");
        if (request == null) {
            request = new LogoutRequest();
        }
        MessageResponse response = authService.logout(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /me
     * Get current authenticated user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.debug("Get current user request received");
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /me
     * Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        log.debug("Update profile request received");
        UserResponse response = authService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /forgot-password
     * Request password reset email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.debug("Forgot password request received for email: {}", maskEmail(request.getEmail()));
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /reset-password
     * Reset password using token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.debug("Reset password request received");
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /verify-email
     * Verify email address using token
     */
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.debug("Email verification request received");
        MessageResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /resend-verification
     * Resend email verification link
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification() {
        log.debug("Resend verification email request received");
        MessageResponse response = authService.resendVerification();
        return ResponseEntity.ok(response);
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
