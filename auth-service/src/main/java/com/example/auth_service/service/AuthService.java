package com.example.auth_service.service;

import com.example.auth_service.config.SecurityProperties;
import com.example.auth_service.config.TokenProperties;
import com.example.auth_service.dto.request.*;
import com.example.auth_service.dto.response.*;
import com.example.auth_service.entity.*;
import com.example.auth_service.exception.*;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.repository.VerificationTokenRepository;
import com.example.auth_service.security.JwtService;
import com.example.auth_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenProperties tokenProperties;
    private final SecurityProperties securityProperties;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Get the default USER role
        Role userRole = roleRepository.findByName(Role.USER)
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));

        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .emailVerified(false)
                .isActive(true)
                .build();

        // Add the default role
        user.addRole(userRole);

        user = userRepository.save(user);
        log.info("User registered successfully: {}", maskEmail(user.getEmail()));

        // Generate email verification token
        String verificationToken = generateVerificationToken(user, TokenType.EMAIL_VERIFICATION,
                tokenProperties.getEmailVerificationExpiry());

        // Send verification email asynchronously
       // emailService.sendVerificationEmail(user, verificationToken);

        return RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.getEmailVerified())
                .roles(user.getRoleNames())
                .message("Verification email sent")
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        // Check if user exists and is not locked
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.isAccountLocked()) {
            log.warn("Login attempt for locked account: {}", maskEmail(email));
            throw new AccountLockedException();
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            // Reset failed login attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                userRepository.resetLoginAttempts(user.getId());
            }

            // Update last login
            userRepository.updateLastLoginAt(user.getId(), Instant.now());

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Store refresh token
            saveRefreshToken(user, refreshToken);

            log.info("User logged in successfully: {}", maskEmail(email));

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAccessTokenExpiry() / 1000)
                    .user(UserResponse.fromUserBasic(user))
                    .build();

        } catch (BadCredentialsException | LockedException e) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException();
        }
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        // Validate refresh token
        if (!jwtService.validateToken(refreshTokenStr) || !jwtService.isRefreshToken(refreshTokenStr)) {
            throw InvalidTokenException.invalid();
        }

        // Check if token exists and is not revoked
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(InvalidTokenException::invalid);

        if (!storedToken.isValid()) {
            throw storedToken.getIsRevoked() ? InvalidTokenException.revoked() : InvalidTokenException.expired();
        }

        User user = storedToken.getUser();

        // Revoke old refresh token (token rotation)
        storedToken.setIsRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Store new refresh token
        saveRefreshToken(user, newRefreshToken);

        log.info("Token refreshed for user: {}", maskEmail(user.getEmail()));

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiry() / 1000)
                .build();
    }

    @Transactional
    public MessageResponse logout(LogoutRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            // Revoke specific refresh token if provided
            if (request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
                refreshTokenRepository.revokeByToken(request.getRefreshToken());
            } else {
                // Revoke all refresh tokens for the user
                refreshTokenRepository.revokeAllByUserId(principal.getId());
            }
            log.info("User logged out: {}", maskEmail(principal.getEmail()));
        }

        return MessageResponse.of("Logged out successfully");
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        UserPrincipal principal = getCurrentUserPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(UserNotFoundException::new);

        return UserResponse.fromUser(user);
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        UserPrincipal principal = getCurrentUserPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(UserNotFoundException::new);

        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPreferences() != null) {
            user.setPreferences(request.getPreferences());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", maskEmail(user.getEmail()));

        return UserResponse.fromUser(user);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        // Always return success to prevent email enumeration
        userRepository.findByEmail(email).ifPresent(user -> {
            // Invalidate existing password reset tokens
            verificationTokenRepository.invalidateTokensByUserAndType(user.getId(), TokenType.PASSWORD_RESET);

            // Generate new token
            String token = generateVerificationToken(user, TokenType.PASSWORD_RESET,
                    tokenProperties.getPasswordResetExpiry());

            // Send email
            emailService.sendPasswordResetEmail(user, token);
            log.info("Password reset email sent to: {}", maskEmail(email));
        });

        return MessageResponse.of("If the email exists, a password reset link has been sent");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByTokenAndTokenType(request.getToken(), TokenType.PASSWORD_RESET)
                .orElseThrow(InvalidTokenException::invalid);

        if (!verificationToken.isValid()) {
            throw verificationToken.getUsed() ? InvalidTokenException.alreadyUsed() : InvalidTokenException.expired();
        }

        User user = verificationToken.getUser();

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(user);

        log.info("Password reset successful for user: {}", maskEmail(user.getEmail()));

        return MessageResponse.of("Password reset successful");
    }

    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByTokenAndTokenType(request.getToken(), TokenType.EMAIL_VERIFICATION)
                .orElseThrow(InvalidTokenException::invalid);

        if (!verificationToken.isValid()) {
            throw verificationToken.getUsed() ? InvalidTokenException.alreadyUsed() : InvalidTokenException.expired();
        }

        User user = verificationToken.getUser();

        // Verify email
        user.setEmailVerified(true);
        userRepository.save(user);

        // Mark token as used
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        log.info("Email verified for user: {}", maskEmail(user.getEmail()));

        return MessageResponse.of("Email verified successfully");
    }

    @Transactional
    public MessageResponse resendVerification() {
        UserPrincipal principal = getCurrentUserPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(UserNotFoundException::new);

        if (user.getEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        // Check rate limit (max 3 per hour)
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        long recentTokens = verificationTokenRepository.countRecentTokensByUserAndType(
                user.getId(), TokenType.EMAIL_VERIFICATION, oneHourAgo);

        if (recentTokens >= 3) {
            throw new RateLimitExceededException("Maximum verification emails sent. Please try again later.");
        }

        // Invalidate existing tokens
        verificationTokenRepository.invalidateTokensByUserAndType(user.getId(), TokenType.EMAIL_VERIFICATION);

        // Generate new token
        String token = generateVerificationToken(user, TokenType.EMAIL_VERIFICATION,
                tokenProperties.getEmailVerificationExpiry());

        // Send email
        emailService.sendVerificationEmail(user, token);

        log.info("Verification email resent to: {}", maskEmail(user.getEmail()));

        return MessageResponse.of("Verification email sent");
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        Instant lockedUntil = null;

        if (attempts >= securityProperties.getLogin().getMaxAttempts()) {
            lockedUntil = Instant.now().plusMillis(securityProperties.getLogin().getLockoutDuration());
            log.warn("Account locked due to failed login attempts: {}", maskEmail(user.getEmail()));
        }

        userRepository.updateLoginAttempts(user.getId(), attempts, lockedUntil);
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(jwtService.getRefreshTokenExpiryInstant())
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private String generateVerificationToken(User user, TokenType tokenType, Long expiryMs) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(token)
                .tokenType(tokenType)
                .expiresAt(Instant.now().plusMillis(expiryMs))
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        return token;
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new InvalidCredentialsException("Not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
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
