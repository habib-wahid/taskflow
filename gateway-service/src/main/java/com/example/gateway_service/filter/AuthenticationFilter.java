package com.example.gateway_service.filter;

import com.example.gateway_service.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter {

    private final JwtUtil jwtUtil;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_EMAIL = "X-User-Email";
    private static final String X_USER_ROLES = "X-User-Roles";

    public ServerRequest filter(ServerRequest request) {
        log.debug("Processing authentication for request: {}", request.path());

        String authHeader = request.headers().firstHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header");
            throw new AuthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token");
                throw new AuthenticationException("Invalid JWT token");
            }

            Claims claims = jwtUtil.extractAllClaims(token);
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            List<String> roles = claims.get("roles", List.class);

            log.debug("Authenticated user: {} ({})", userId, email);

            // Create new request with added headers
            return ServerRequest.from(request)
                    .header(X_USER_ID, userId)
                    .header(X_USER_EMAIL, email != null ? email : "")
                    .header(X_USER_ROLES, roles != null ? roles.get(0) : "")
                    .build();

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
            throw new AuthenticationException("Error processing authentication");
        }
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }

        public HttpStatus getStatus() {
            return HttpStatus.UNAUTHORIZED;
        }
    }
}
