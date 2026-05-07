package com.checkpoint.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for completing the 2FA login step.
 * Desktop clients send the intermediate token in the body; web clients rely on the cookie.
 */
public record TwoFactorLoginRequestDto(
        String intermediateToken,
        @NotBlank(message = "Code is required") String code
) {}
