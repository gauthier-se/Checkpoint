package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.Like;

/**
 * Repository for Like entity.
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {

    /**
     * Counts the number of likes for a game list.
     */
    long countByGameListId(UUID gameListId);

    /**
     * Checks if a user has liked a game list.
     */
    boolean existsByUserIdAndGameListId(UUID userId, UUID gameListId);

    /**
     * Finds a like by user and game list.
     */
    Optional<Like> findByUserIdAndGameListId(UUID userId, UUID gameListId);

    /**
     * Counts the number of likes for a review.
     */
    long countByReviewId(UUID reviewId);

    /**
     * Checks if a user has liked a review.
     */
    boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);

    /**
     * Finds a like by user and review.
     */
    Optional<Like> findByUserIdAndReviewId(UUID userId, UUID reviewId);

    /**
     * Counts the number of likes for a comment.
     */
    long countByCommentId(UUID commentId);

    /**
     * Checks if a user has liked a comment.
     */
    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);

    /**
     * Finds a like by user and comment.
     */
    Optional<Like> findByUserIdAndCommentId(UUID userId, UUID commentId);
}
