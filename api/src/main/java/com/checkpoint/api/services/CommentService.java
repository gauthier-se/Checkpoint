package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.social.CommentResponseDto;

/**
 * Service for managing comments on reviews and game lists.
 */
public interface CommentService {

    /**
     * Adds a comment to a review.
     *
     * @param userEmail the authenticated user's email
     * @param reviewId  the review ID
     * @param content   the comment text content
     * @return the created comment
     * @throws com.checkpoint.api.exceptions.ReviewNotFoundException if the review does not exist
     */
    CommentResponseDto addReviewComment(String userEmail, UUID reviewId, String content);

    /**
     * Adds a comment to a game list.
     *
     * @param userEmail the authenticated user's email
     * @param listId    the game list ID
     * @param content   the comment text content
     * @return the created comment
     * @throws com.checkpoint.api.exceptions.GameListNotFoundException if the game list does not exist
     */
    CommentResponseDto addListComment(String userEmail, UUID listId, String content);

    /**
     * Retrieves a paginated list of top-level comments for a review.
     *
     * @param reviewId    the review ID
     * @param viewerEmail the viewer's email (nullable, for hasLiked resolution)
     * @param pageable    pagination and sorting details
     * @return a page of comments enriched with like/reply counts
     */
    Page<CommentResponseDto> getReviewComments(UUID reviewId, String viewerEmail, Pageable pageable);

    /**
     * Retrieves a paginated list of top-level comments for a game list.
     *
     * @param listId      the game list ID
     * @param viewerEmail the viewer's email (nullable, for hasLiked resolution)
     * @param pageable    pagination and sorting details
     * @return a page of comments enriched with like/reply counts
     */
    Page<CommentResponseDto> getListComments(UUID listId, String viewerEmail, Pageable pageable);

    /**
     * Adds a reply to an existing comment. Enforces 1-level nesting:
     * replying to a reply targets the root parent.
     *
     * @param userEmail       the authenticated user's email
     * @param parentCommentId the parent comment ID
     * @param content         the reply text content
     * @return the created reply
     * @throws com.checkpoint.api.exceptions.CommentNotFoundException if the parent comment does not exist
     */
    CommentResponseDto addReply(String userEmail, UUID parentCommentId, String content);

    /**
     * Retrieves a paginated list of replies for a comment.
     *
     * @param parentCommentId the parent comment ID
     * @param viewerEmail     the viewer's email (nullable, for hasLiked resolution)
     * @param pageable        pagination and sorting details
     * @return a page of reply comments enriched with like counts
     */
    Page<CommentResponseDto> getReplies(UUID parentCommentId, String viewerEmail, Pageable pageable);

    /**
     * Updates a comment. Only the comment owner can perform this action.
     *
     * @param userEmail the authenticated user's email
     * @param commentId the comment ID
     * @param content   the updated comment text content
     * @return the updated comment
     * @throws com.checkpoint.api.exceptions.CommentNotFoundException          if the comment does not exist
     * @throws com.checkpoint.api.exceptions.UnauthorizedCommentAccessException if the user is not the comment owner
     */
    CommentResponseDto updateComment(String userEmail, UUID commentId, String content);

    /**
     * Deletes a comment. Only the comment owner can perform this action.
     *
     * @param userEmail the authenticated user's email
     * @param commentId the comment ID
     * @throws com.checkpoint.api.exceptions.CommentNotFoundException          if the comment does not exist
     * @throws com.checkpoint.api.exceptions.UnauthorizedCommentAccessException if the user is not the comment owner
     */
    void deleteComment(String userEmail, UUID commentId);
}
