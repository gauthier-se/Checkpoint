 package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.admin.AdminReportedReviewDto;
import com.checkpoint.api.dto.admin.AdminReviewDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;

/**
 * Service interface for admin review management operations.
 */
public interface AdminReviewService {

    /**
     * Retrieves a paginated list of all reviews.
     *
     * @param pageable pagination and sorting details
     * @return the paginated reviews
     */
    PagedResponseDto<AdminReviewDto> getAllReviews(Pageable pageable);

    /**
     * Retrieves a paginated list of reviews that have been reported at least once.
     *
     * @param pageable pagination and sorting details
     * @return the paginated reported reviews with their report counts
     */
    PagedResponseDto<AdminReportedReviewDto> getReportedReviews(Pageable pageable);

    /**
     * Deletes a review by its ID.
     *
     * @param reviewId the ID of the review to delete
     */
    void deleteReview(UUID reviewId);
}
