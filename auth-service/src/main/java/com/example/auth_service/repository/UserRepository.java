package com.example.auth_service.repository;

import com.example.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") UUID userId, @Param("lastLoginAt") Instant lastLoginAt);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts, u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void updateLoginAttempts(@Param("userId") UUID userId, @Param("attempts") Integer attempts, @Param("lockedUntil") Instant lockedUntil);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :userId")
    void resetLoginAttempts(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void verifyEmail(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId, @Param("passwordHash") String passwordHash);
}
