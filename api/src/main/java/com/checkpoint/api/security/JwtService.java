package com.checkpoint.api.security;

import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service interface for JWT token operations.
 */
public interface JwtService {

    /**
     * Generates a JWT token for the given user details.
     *
     * @param userDetails the user details
     * @return the generated JWT token
     */
    String generateToken(UserDetails userDetails);

    /**
     * Generates a JWT token with extra claims for the given user details.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details
     * @return the generated JWT token
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    String extractUsername(String token);

    /**
     * Validates whether the given JWT token is valid for the given user details.
     *
     * @param token       the JWT token
     * @param userDetails the user details to validate against
     * @return true if the token is valid
     */
    boolean isTokenValid(String token, UserDetails userDetails);
}
