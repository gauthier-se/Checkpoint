package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.ReportRequestDto;
import com.checkpoint.api.dto.catalog.ReportResponseDto;
import com.checkpoint.api.entities.Report;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.DuplicateReportException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.repositories.ReportRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.ReportService;

/**
 * Implementation of {@link ReportService}.
 * Manages review reports created by authenticated users.
 */
@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new ReportServiceImpl.
     *
     * @param reportRepository the report repository
     * @param reviewRepository the review repository
     * @param userRepository   the user repository
     */
    public ReportServiceImpl(ReportRepository reportRepository,
                             ReviewRepository reviewRepository,
                             UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportResponseDto reportReview(String userEmail, UUID reviewId, ReportRequestDto request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        if (reportRepository.existsByUserIdAndReviewId(user.getId(), reviewId)) {
            throw new DuplicateReportException(reviewId);
        }

        Report report = Report.onReview(request.content(), user, review);
        Report savedReport = reportRepository.save(report);

        log.info("Created report on review {} by user {}", reviewId, user.getPseudo());

        return new ReportResponseDto(
                savedReport.getId(),
                savedReport.getContent(),
                savedReport.getCreatedAt()
        );
    }
}
