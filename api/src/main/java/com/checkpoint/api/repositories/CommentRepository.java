package com.checkpoint.api.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.Comment;

/**
 * Repository for {@link Comment} entity.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Finds all comments for a specific review.
     *
     * @param reviewId the review ID
     * @param pageable pagination and sorting details
     * @return a page of comments
     */
    Page<Comment> findByReviewId(UUID reviewId, Pageable pageable);

    /**
     * Finds all comments for a specific game list.
     *
     * @param gameListId the game list ID
     * @param pageable   pagination and sorting details
     * @return a page of comments
     */
    Page<Comment> findByGameListId(UUID gameListId, Pageable pageable);

    /**
     * Counts the number of comments for a review.
     *
     * @param reviewId the review ID
     * @return the comment count
     */
    long countByReviewId(UUID reviewId);

    /**
     * Counts the number of comments for a game list.
     *
     * @param gameListId the game list ID
     * @return the comment count
     */
    long countByGameListId(UUID gameListId);
}
