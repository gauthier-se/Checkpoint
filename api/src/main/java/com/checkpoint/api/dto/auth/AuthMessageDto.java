package com.checkpoint.api.dto.auth;

/**
 * Simple message response for auth operations (e.g. logout, session login).
 *
 * @param message descriptive message about the result
 */
public record AuthMessageDto(
        String message
) {}
