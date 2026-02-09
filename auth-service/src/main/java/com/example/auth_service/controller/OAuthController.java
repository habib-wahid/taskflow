//package com.example.auth_service.controller;
//
//import com.example.auth_service.config.AppProperties;
//import com.example.auth_service.dto.response.AuthResponse;
//import com.example.auth_service.service.OAuthService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.URI;
//
//@Slf4j
//@RestController
//@RequestMapping("/oauth")
//@RequiredArgsConstructor
//public class OAuthController {
//
//    private final OAuthService oauthService;
//    private final AppProperties appProperties;
//
//    /**
//     * GET /oauth/{provider}
//     * Initiate OAuth2 authentication flow
//     */
//    @GetMapping("/{provider}")
//    public ResponseEntity<Void> initiateOAuth(@PathVariable String provider) {
//        log.debug("OAuth initiation request for provider: {}", provider);
//
//        String authorizationUrl = oauthService.getAuthorizationUrl(provider.toLowerCase());
//
//        return ResponseEntity.status(HttpStatus.FOUND)
//                .location(URI.create(authorizationUrl))
//                .build();
//    }
//
//    /**
//     * GET /oauth/{provider}/callback
//     * OAuth2 callback handler
//     */
//    @GetMapping("/{provider}/callback")
//    public ResponseEntity<Void> handleCallback(
//            @PathVariable String provider,
//            @RequestParam String code,
//            @RequestParam String state) {
//
//        log.debug("OAuth callback received for provider: {}", provider);
//
//        AuthResponse authResponse = oauthService.handleCallback(provider.toLowerCase(), code, state);
//
//        String redirectUrl = oauthService.getFrontendRedirectUrl(
//                authResponse.getAccessToken(),
//                authResponse.getRefreshToken()
//        );
//
//        return ResponseEntity.status(HttpStatus.FOUND)
//                .location(URI.create(redirectUrl))
//                .build();
//    }
//
//    /**
//     * POST /oauth/{provider}/token
//     * Alternative endpoint to get tokens via POST (for SPA clients)
//     */
//    @PostMapping("/{provider}/token")
//    public ResponseEntity<AuthResponse> handleTokenRequest(
//            @PathVariable String provider,
//            @RequestParam String code,
//            @RequestParam String state) {
//
//        log.debug("OAuth token request received for provider: {}", provider);
//
//        AuthResponse authResponse = oauthService.handleCallback(provider.toLowerCase(), code, state);
//
//        return ResponseEntity.ok(authResponse);
//    }
//}
