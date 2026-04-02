package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a comment is not found.
 */
public class CommentNotFoundException extends RuntimeException {

    private final UUID commentId;

    public CommentNotFoundException(UUID commentId) {
        super("Comment not found with ID: " + commentId);
        this.commentId = commentId;
    }

    public UUID getCommentId() {
        return commentId;
    }
}
