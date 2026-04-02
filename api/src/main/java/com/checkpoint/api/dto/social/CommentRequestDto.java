package com.checkpoint.api.dto.social;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a comment.
 *
 * @param content the comment text content
 */
public record CommentRequestDto(
        @NotBlank(message = "Comment content must not be blank")
        @Size(max = 5000, message = "Comment content must not exceed 5000 characters")
        String content
) {}
