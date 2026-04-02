package com.checkpoint.api.dto.social;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a comment returned to the client.
 *
 * @param id        the comment ID
 * @param content   the comment text content
 * @param user      the comment author
 * @param createdAt when the comment was created
 * @param updatedAt when the comment was last updated
 */
public record CommentResponseDto(
        UUID id,
        String content,
        CommentUserDto user,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
