package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.dto.catalog.ReportRequestDto;
import com.checkpoint.api.dto.catalog.ReportResponseDto;

/**
 * Service for managing review reports.
 */
public interface ReportService {

    /**
     * Reports a review.
     *
     * @param userEmail the authenticated user's email
     * @param reviewId  the review to report
     * @param request   the report request containing the reason
     * @return the created report
     * @throws com.checkpoint.api.exceptions.ReviewNotFoundException   if the review does not exist
     * @throws com.checkpoint.api.exceptions.DuplicateReportException  if the user already reported this review
     */
    ReportResponseDto reportReview(String userEmail, UUID reviewId, ReportRequestDto request);
}
