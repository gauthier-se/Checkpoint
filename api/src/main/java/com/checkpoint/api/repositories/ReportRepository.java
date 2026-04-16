package com.checkpoint.api.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.checkpoint.api.entities.Report;

/**
 * Repository for {@link Report} entity.
 */
public interface ReportRepository extends JpaRepository<Report, UUID> {

    /**
     * Checks whether a report already exists from a specific user on a specific review.
     *
     * @param userId   the user's ID
     * @param reviewId the review's ID
     * @return true if a report already exists
     */
    boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);

    /**
     * Finds all reports where the reported content is a review (review is not null).
     *
     * @param pageable pagination and sorting details
     * @return a page of reports targeting reviews
     */
    Page<Report> findByReviewIsNotNull(Pageable pageable);

    /**
     * Finds all reports where the reported content is a comment (comment is not null).
     *
     * @param pageable pagination and sorting details
     * @return a page of reports targeting comments
     */
    Page<Report> findByCommentIsNotNull(Pageable pageable);
}
