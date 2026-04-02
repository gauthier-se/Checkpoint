package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a user tries to modify or delete a comment they do not own.
 */
public class UnauthorizedCommentAccessException extends RuntimeException {

    private final UUID commentId;

    public UnauthorizedCommentAccessException(UUID commentId) {
        super("You do not have permission to modify comment with ID: " + commentId);
        this.commentId = commentId;
    }

    public UUID getCommentId() {
        return commentId;
    }
}
