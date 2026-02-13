package com.example.gateway_service.exception;

import com.example.gateway_service.filter.AuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationFilter.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationFilter.AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());

        Map<String, Object> response = Map.of(
                "success", false,
                "message", ex.getMessage(),
                "errorCode", "UNAUTHORIZED",
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Gateway error: ", ex);

        Map<String, Object> response = Map.of(
                "success", false,
                "message", "Gateway error occurred",
                "errorCode", "GATEWAY_ERROR",
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
