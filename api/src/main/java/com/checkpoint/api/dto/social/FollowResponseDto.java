package com.checkpoint.api.dto.social;

/**
 * DTO for the follow/unfollow toggle response.
 *
 * @param following whether the authenticated user is now following the target user
 * @param message   a human-readable message describing the action taken
 */
public record FollowResponseDto(
        boolean following,
        String message
) {}
