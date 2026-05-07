package com.checkpoint.api.dto.auth;

/**
 * Response DTO for the 2FA setup endpoint.
 * Contains the provisioning URI for QR code generation and a pre-rendered QR code data URL.
 */
public record TwoFactorSetupResponseDto(
        String provisioningUri,
        String qrCodeDataUrl
) {}
