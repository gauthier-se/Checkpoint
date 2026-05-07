package com.checkpoint.api.services.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.entities.RefreshToken;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.InvalidRefreshTokenException;
import com.checkpoint.api.repositories.RefreshTokenRepository;
import com.checkpoint.api.services.RefreshTokenService;

/**
 * Implementation of {@link RefreshTokenService}.
 * Manages the lifecycle of opaque refresh tokens stored in the {@code refresh_tokens} table.
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L);
        RefreshToken refreshToken = new RefreshToken(tokenValue, user, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is required");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Override
    public void revokeToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        Optional<RefreshToken> existing = refreshTokenRepository.findByToken(token);
        existing.ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
