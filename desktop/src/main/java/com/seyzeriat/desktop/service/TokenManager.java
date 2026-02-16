package com.seyzeriat.desktop.service;

/**
 * Secure in-memory JWT token manager (singleton).
 *
 * <p>Stores the JWT token in a volatile field so it is never persisted to disk.
 * The token is lost when the application exits, forcing re-authentication on
 * the next launch.</p>
 */
public final class TokenManager {

    private static final TokenManager INSTANCE = new TokenManager();

    private volatile String token;

    private TokenManager() {}

    public static TokenManager getInstance() {
        return INSTANCE;
    }

    /**
     * Stores the JWT token.
     *
     * @param token the JWT access token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns the stored JWT token, or {@code null} if not authenticated.
     */
    public String getToken() {
        return token;
    }

    /**
     * Clears the stored token (logout / session expiry).
     */
    public void clear() {
        this.token = null;
    }

    /**
     * Returns {@code true} if a token is currently stored.
     */
    public boolean isAuthenticated() {
        return token != null && !token.isBlank();
    }
}
