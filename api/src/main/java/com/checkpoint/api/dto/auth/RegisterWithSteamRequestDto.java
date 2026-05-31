package com.checkpoint.api.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/v1/auth/register/steam}. The {@code token} is a short-lived
 * Steam signup JWT issued by {@code /api/v1/auth/steam/openid/callback} when no CheckPoint
 * account is linked to the verified SteamID. {@code password} is optional: a Steam-only
 * account is created when it is {@code null} or blank.
 *
 * @param token        the Steam signup token (required)
 * @param email        the email the user wants to register with (required)
 * @param pseudo       the username the user wants to register with (required)
 * @param acceptTerms  must be {@code true}: the user accepts the ToS and Privacy Policy
 * @param password     optional password; when provided must be at least 8 characters long
 */
public record RegisterWithSteamRequestDto(
        @NotBlank(message = "Steam signup token is required")
        String token,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Pseudo is required")
        String pseudo,

        @AssertTrue(message = "You must accept the Terms of Service and Privacy Policy")
        boolean acceptTerms,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {
}
