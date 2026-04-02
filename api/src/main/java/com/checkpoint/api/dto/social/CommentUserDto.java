package com.checkpoint.api.dto.social;

import java.util.UUID;

/**
 * DTO containing basic user information for a comment.
 *
 * @param id      the user ID
 * @param pseudo  the user's display name
 * @param picture the user's profile picture URL
 */
public record CommentUserDto(
        UUID id,
        String pseudo,
        String picture
) {}
