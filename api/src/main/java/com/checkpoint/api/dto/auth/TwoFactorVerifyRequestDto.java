package com.checkpoint.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for verifying the first TOTP code and enabling 2FA.
 */
public record TwoFactorVerifyRequestDto(
        @NotBlank(message = "Code is required") String code
) {}
