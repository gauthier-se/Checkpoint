package com.checkpoint.api.repositories;

import java.util.UUID;

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
}
