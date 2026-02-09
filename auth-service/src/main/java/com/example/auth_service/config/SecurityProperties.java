package com.example.auth_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private Password password = new Password();
    private Login login = new Login();
    private RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class Password {
        private Integer bcryptStrength = 12;
        private Integer minLength = 8;
    }

    @Getter
    @Setter
    public static class Login {
        private Integer maxAttempts = 5;
        private Long lockoutDuration = 900000L;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private Integer requestsPerWindow = 100;
        private Long windowDuration = 900000L;
    }
}
