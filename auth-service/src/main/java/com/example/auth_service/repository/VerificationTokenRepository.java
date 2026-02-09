package com.example.auth_service.repository;

import com.example.auth_service.entity.TokenType;
import com.example.auth_service.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    @Modifying
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.token = :token")
    void markAsUsed(@Param("token") String token);

    @Modifying
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.user.id = :userId AND vt.tokenType = :tokenType AND vt.used = false")
    void invalidateTokensByUserAndType(@Param("userId") UUID userId, @Param("tokenType") TokenType tokenType);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);

    @Query("SELECT COUNT(vt) FROM VerificationToken vt WHERE vt.user.id = :userId AND vt.tokenType = :tokenType AND vt.createdAt > :since")
    long countRecentTokensByUserAndType(@Param("userId") UUID userId, @Param("tokenType") TokenType tokenType, @Param("since") Instant since);
}
