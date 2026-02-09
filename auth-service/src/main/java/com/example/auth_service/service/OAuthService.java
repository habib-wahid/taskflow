//package com.example.auth_service.service;
//
//import com.example.auth_service.config.AppProperties;
//import com.example.auth_service.dto.response.AuthResponse;
//import com.example.auth_service.dto.response.UserResponse;
//import com.example.auth_service.entity.OAuthProvider;
//import com.example.auth_service.entity.Role;
//import com.example.auth_service.entity.User;
//import com.example.auth_service.exception.UnsupportedOAuthProviderException;
//import com.example.auth_service.repository.OAuthProviderRepository;
//import com.example.auth_service.repository.RefreshTokenRepository;
//import com.example.auth_service.repository.RoleRepository;
//import com.example.auth_service.repository.UserRepository;
//import com.example.auth_service.security.JwtService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class OAuthService {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final OAuthProviderRepository oauthProviderRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final JwtService jwtService;
//    private final AppProperties appProperties;
//    private final ClientRegistrationRepository clientRegistrationRepository;
//
//    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("google", "github", "microsoft");
//    private final Map<String, String> stateStore = new ConcurrentHashMap<>();
//
//    public String getAuthorizationUrl(String provider) {
//        validateProvider(provider);
//
//        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);
//        if (registration == null) {
//            throw new UnsupportedOAuthProviderException(provider);
//        }
//
//        String state = UUID.randomUUID().toString();
//        stateStore.put(state, provider);
//
//        // Schedule state cleanup after 5 minutes
//        scheduleStateCleanup(state);
//
//        return String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&state=%s&response_type=code",
//                registration.getProviderDetails().getAuthorizationUri(),
//                registration.getClientId(),
//                getRedirectUri(provider),
//                String.join(" ", registration.getScopes()),
//                state);
//    }
//
//    @Transactional
//    public AuthResponse handleCallback(String provider, String code, String state) {
//        validateProvider(provider);
//
//        // Validate state
//        String storedProvider = stateStore.remove(state);
//        if (storedProvider == null || !storedProvider.equals(provider)) {
//            throw new IllegalArgumentException("Invalid state parameter");
//        }
//
//        // Exchange code for tokens and get user info
//        OAuthUserInfo userInfo = fetchUserInfo(provider, code);
//
//        // Find or create user
//        User user = findOrCreateUser(provider, userInfo);
//
//        // Generate JWT tokens
//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user);
//
//        // Store refresh token
//        saveRefreshToken(user, refreshToken);
//
//        log.info("OAuth login successful for user: {} via {}", maskEmail(user.getEmail()), provider);
//
//        return AuthResponse.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .tokenType("Bearer")
//                .expiresIn(jwtService.getAccessTokenExpiry() / 1000)
//                .user(UserResponse.fromUserBasic(user))
//                .build();
//    }
//
//    public String getFrontendRedirectUrl(String accessToken, String refreshToken) {
//        return String.format("%s/auth/callback?accessToken=%s&refreshToken=%s",
//                appProperties.getFrontendUrl(), accessToken, refreshToken);
//    }
//
//    private User findOrCreateUser(String provider, OAuthUserInfo userInfo) {
//        // First try to find by OAuth provider
//        Optional<OAuthProvider> existingOAuth = oauthProviderRepository
//                .findByProviderAndProviderUserId(provider, userInfo.getId());
//
//        if (existingOAuth.isPresent()) {
//            User user = existingOAuth.get().getUser();
//            userRepository.updateLastLoginAt(user.getId(), Instant.now());
//            return user;
//        }
//
//        // Try to find by email
//        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail().toLowerCase());
//
//        User user;
//        if (existingUser.isPresent()) {
//            user = existingUser.get();
//            // Link OAuth provider to existing user
//            createOAuthProvider(user, provider, userInfo);
//        } else {
//            // Get the default USER role
//            Role userRole = roleRepository.findByName(Role.USER)
//                    .orElseThrow(() -> new RuntimeException("Default USER role not found"));
//
//            // Create new user
//            user = User.builder()
//                    .email(userInfo.getEmail().toLowerCase())
//                    .firstName(userInfo.getFirstName())
//                    .lastName(userInfo.getLastName())
//                    .avatarUrl(userInfo.getAvatarUrl())
//                    .emailVerified(true) // OAuth emails are pre-verified
//                    .isActive(true)
//                    .build();
//
//            // Add the default role
//            user.addRole(userRole);
//
//            user = userRepository.save(user);
//            createOAuthProvider(user, provider, userInfo);
//
//            log.info("New user created via OAuth: {} provider: {}", maskEmail(user.getEmail()), provider);
//        }
//
//        userRepository.updateLastLoginAt(user.getId(), Instant.now());
//        return user;
//    }
//
//    private void createOAuthProvider(User user, String provider, OAuthUserInfo userInfo) {
//        OAuthProvider oauthProvider = OAuthProvider.builder()
//                .user(user)
//                .provider(provider)
//                .providerUserId(userInfo.getId())
//                .accessToken(userInfo.getAccessToken())
//                .build();
//
//        oauthProviderRepository.save(oauthProvider);
//    }
//
//    private OAuthUserInfo fetchUserInfo(String provider, String code) {
//        // This is a simplified implementation
//        // In production, you would use Spring Security OAuth2 client to handle token exchange
//
//        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);
//
//        // For demonstration, return mock data
//        // In real implementation, exchange code for token and fetch user info from provider
//        return switch (provider) {
//            case "google" -> fetchGoogleUserInfo(registration, code);
//            case "github" -> fetchGitHubUserInfo(registration, code);
//            case "microsoft" -> fetchMicrosoftUserInfo(registration, code);
//            default -> throw new UnsupportedOAuthProviderException(provider);
//        };
//    }
//
//    private OAuthUserInfo fetchGoogleUserInfo(ClientRegistration registration, String code) {
//        // Simplified - in production use RestTemplate or WebClient to exchange code and fetch user info
//        // Exchange authorization code for access token
//        // Then call https://www.googleapis.com/oauth2/v2/userinfo
//
//        // Placeholder implementation
//        throw new UnsupportedOperationException("Google OAuth implementation requires proper token exchange");
//    }
//
//    private OAuthUserInfo fetchGitHubUserInfo(ClientRegistration registration, String code) {
//        // Exchange code for token at https://github.com/login/oauth/access_token
//        // Then call https://api.github.com/user
//
//        // Placeholder implementation
//        throw new UnsupportedOperationException("GitHub OAuth implementation requires proper token exchange");
//    }
//
//    private OAuthUserInfo fetchMicrosoftUserInfo(ClientRegistration registration, String code) {
//        // Exchange code for token at https://login.microsoftonline.com/common/oauth2/v2.0/token
//        // Then call https://graph.microsoft.com/oidc/userinfo
//
//        // Placeholder implementation
//        throw new UnsupportedOperationException("Microsoft OAuth implementation requires proper token exchange");
//    }
//
//    private void validateProvider(String provider) {
//        if (!SUPPORTED_PROVIDERS.contains(provider.toLowerCase())) {
//            throw new UnsupportedOAuthProviderException(provider);
//        }
//    }
//
//    private String getRedirectUri(String provider) {
//        return appProperties.getFrontendUrl() + "/api/auth/oauth/" + provider + "/callback";
//    }
//
//    private void saveRefreshToken(User user, String token) {
//        var refreshToken = com.example.auth_service.entity.RefreshToken.builder()
//                .user(user)
//                .token(token)
//                .expiresAt(jwtService.getRefreshTokenExpiryInstant())
//                .isRevoked(false)
//                .build();
//
//        refreshTokenRepository.save(refreshToken);
//    }
//
//    private void scheduleStateCleanup(String state) {
//        // In production, use a scheduled task or Redis with TTL
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                stateStore.remove(state);
//            }
//        }, 5 * 60 * 1000); // 5 minutes
//    }
//
//    private String maskEmail(String email) {
//        if (email == null || !email.contains("@")) {
//            return "***";
//        }
//        int atIndex = email.indexOf("@");
//        if (atIndex <= 2) {
//            return "***" + email.substring(atIndex);
//        }
//        return email.substring(0, 2) + "***" + email.substring(atIndex);
//    }
//
//    // Inner class for OAuth user info
//    public record OAuthUserInfo(
//            String id,
//            String email,
//            String firstName,
//            String lastName,
//            String avatarUrl,
//            String accessToken
//    ) {
//        public String getId() { return id; }
//        public String getEmail() { return email; }
//        public String getFirstName() { return firstName != null ? firstName : ""; }
//        public String getLastName() { return lastName != null ? lastName : ""; }
//        public String getAvatarUrl() { return avatarUrl; }
//        public String getAccessToken() { return accessToken; }
//    }
//}
