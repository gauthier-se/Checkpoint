package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.entities.RefreshToken;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.InvalidRefreshTokenException;

/**
 * Service interface for refresh token lifecycle management.
 */
public interface RefreshTokenService {

    /**
     * Creates and persists a new refresh token for the given user.
     *
     * @param user the user to associate the token with
     * @return the persisted {@link RefreshToken}
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Validates a refresh token and returns the entity.
     * Throws {@link InvalidRefreshTokenException} if the token is not found, revoked, or expired.
     *
     * @param token the raw token string
     * @return the validated {@link RefreshToken} entity
     */
    RefreshToken validateToken(String token);

    /**
     * Marks the given refresh token as revoked. Silently ignores unknown tokens.
     *
     * @param token the raw token string
     */
    void revokeToken(String token);

    /**
     * Revokes all active refresh tokens for the given user.
     *
     * @param userId the user's ID
     */
    void revokeAllForUser(UUID userId);
}
