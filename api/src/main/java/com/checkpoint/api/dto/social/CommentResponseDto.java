package com.checkpoint.api.dto.social;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a comment returned to the client.
 *
 * @param id              the comment ID
 * @param content         the comment text content
 * @param user            the comment author
 * @param createdAt       when the comment was created
 * @param updatedAt       when the comment was last updated
 * @param parentCommentId the parent comment ID (null for top-level comments)
 * @param repliesCount    the number of replies on this comment
 * @param likesCount      the number of likes on this comment
 * @param hasLiked        whether the current viewer has liked this comment
 */
public record CommentResponseDto(
        UUID id,
        String content,
        CommentUserDto user,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID parentCommentId,
        long repliesCount,
        long likesCount,
        boolean hasLiked
) {}
