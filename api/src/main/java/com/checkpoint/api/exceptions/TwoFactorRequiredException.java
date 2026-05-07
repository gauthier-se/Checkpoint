package com.checkpoint.api.exceptions;

/**
 * Thrown during login when a user has 2FA enabled.
 * Carries the intermediate token for Desktop clients (null for Web clients,
 * which receive the token via a cookie instead).
 */
public class TwoFactorRequiredException extends RuntimeException {

    private final String intermediateToken;

    public TwoFactorRequiredException(String intermediateToken) {
        super("Two-factor authentication is required");
        this.intermediateToken = intermediateToken;
    }

    public String getIntermediateToken() {
        return intermediateToken;
    }
}
