package com.checkpoint.api.services;

import com.checkpoint.api.dto.auth.TwoFactorSetupResponseDto;

/**
 * Service interface for TOTP-based two-factor authentication operations.
 */
public interface TwoFactorService {

    /**
     * Generates a new TOTP secret for the user and stores it (without enabling 2FA yet).
     * Returns the provisioning URI and a QR code data URL for scanning in an authenticator app.
     *
     * @param email the authenticated user's email
     * @return setup response containing the provisioning URI and QR code data URL
     */
    TwoFactorSetupResponseDto setup(String email);

    /**
     * Verifies the provided TOTP code against the user's stored secret.
     * If valid, sets {@code twoFactorEnabled = true} on the user.
     *
     * @param email the authenticated user's email
     * @param code  the 6-digit TOTP code from the authenticator app
     */
    void verifyAndEnable(String email, String code);

    /**
     * Disables 2FA for the user after verifying their current password and a valid TOTP code.
     *
     * @param email    the authenticated user's email
     * @param password the user's current plain-text password
     * @param code     the 6-digit TOTP code from the authenticator app
     */
    void disable(String email, String password, String code);

    /**
     * Generates a short-lived (5 min) intermediate JWT for the 2FA login step.
     * The token carries a {@code type: "2fa_intermediate"} claim and is not
     * accepted by the regular JWT authentication filter.
     *
     * @param email the user's email
     * @return the intermediate JWT string
     */
    String generateIntermediateToken(String email);

    /**
     * Validates the intermediate JWT and returns the email it encodes.
     * Throws {@link com.checkpoint.api.exceptions.InvalidTokenException} if the token
     * is missing, expired, or not of type {@code 2fa_intermediate}.
     *
     * @param token the intermediate JWT string
     * @return the user's email extracted from the token
     */
    String resolveEmailFromIntermediateToken(String token);

    /**
     * Verifies a TOTP code against the provided Base32-encoded secret.
     * Allows a ±1-step window to account for clock skew.
     *
     * @param secret the Base32-encoded TOTP secret
     * @param code   the 6-digit code to verify
     * @return true if the code is valid within the time window
     */
    boolean verifyCode(String secret, String code);
}
