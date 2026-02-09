package com.example.auth_service.repository;

import com.example.auth_service.entity.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthProviderRepository extends JpaRepository<OAuthProvider, UUID> {

    Optional<OAuthProvider> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<OAuthProvider> findByUserIdAndProvider(UUID userId, String provider);

    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}
