package com.checkpoint.api.services;

import java.util.UUID;

/**
 * Service interface for admin comment management operations.
 */
public interface AdminCommentService {

    /**
     * Deletes a comment by its ID without ownership check.
     *
     * @param commentId the ID of the comment to delete
     * @throws com.checkpoint.api.exceptions.CommentNotFoundException if the comment does not exist
     */
    void deleteComment(UUID commentId);
}
