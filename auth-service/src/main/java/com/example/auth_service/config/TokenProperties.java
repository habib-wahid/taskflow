package com.example.auth_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "token")
public class TokenProperties {

    private Long emailVerificationExpiry;
    private Long passwordResetExpiry;
}
