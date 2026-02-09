package com.example.auth_service.scheduler;

import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    /**
     * Clean up expired tokens every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting expired token cleanup...");

        Instant now = Instant.now();

        // Clean up expired refresh tokens
        refreshTokenRepository.deleteExpiredTokens(now);

        // Clean up expired verification tokens
        verificationTokenRepository.deleteExpiredTokens(now);

        log.info("Expired token cleanup completed");
    }
}
