package com.checkpoint.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for disabling 2FA. Requires the current password and a valid TOTP code.
 */
public record TwoFactorDisableRequestDto(
        @NotBlank(message = "Password is required") String password,
        @NotBlank(message = "Code is required") String code
) {}
