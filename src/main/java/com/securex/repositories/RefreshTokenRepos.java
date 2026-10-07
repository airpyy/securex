package com.securex.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securex.entity.RefreshToken;

public interface RefreshTokenRepos extends JpaRepository<RefreshToken, UUID> {
Optional<RefreshToken> findByJti(String jti);
}
