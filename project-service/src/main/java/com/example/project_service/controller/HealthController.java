package com.example.project_service.controller;

import com.example.project_service.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> health = Map.of(
                "service", "project-service",
                "status", "UP"
        );
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", health));
    }
}
