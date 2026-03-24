package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.admin.AdminReportedReviewDto;
import com.checkpoint.api.dto.admin.AdminReviewDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.services.AdminReviewService;

@Service
@Transactional
public class AdminReviewServiceImpl implements AdminReviewService {

    private static final Logger log = LoggerFactory.getLogger(AdminReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;

    public AdminReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<AdminReviewDto> getAllReviews(Pageable pageable) {
        log.info("Fetching all reviews for admin (pageable = {})", pageable);

        Page<Review> reviewsPage = reviewRepository.findAll(pageable);

        Page<AdminReviewDto> dtoPage = reviewsPage.map(review -> new AdminReviewDto(
                review.getId(),
                review.getContent(),
                review.getHaveSpoilers(),
                review.getUser() != null ? review.getUser().getPseudo() : null,
                review.getVideoGame() != null ? review.getVideoGame().getTitle() : null,
                review.getCreatedAt()
        ));

        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<AdminReportedReviewDto> getReportedReviews(Pageable pageable) {
        log.info("Fetching reported reviews for admin (pageable = {})", pageable);

        Page<Review> reviewsPage = reviewRepository.findByReportsIsNotEmpty(pageable);

        Page<AdminReportedReviewDto> dtoPage = reviewsPage.map(review -> new AdminReportedReviewDto(
                review.getId(),
                review.getContent(),
                review.getUser() != null ? review.getUser().getPseudo() : null,
                review.getVideoGame() != null ? review.getVideoGame().getTitle() : null,
                review.getReports().size(),
                review.getCreatedAt()
        ));

        return PagedResponseDto.from(dtoPage);
    }

    @Override
    public void deleteReview(UUID reviewId) {
        log.info("Deleting review with id: {}", reviewId);

        if (!reviewRepository.existsById(reviewId)) {
            throw new IllegalArgumentException("Review not found with id: " + reviewId);
        }

        reviewRepository.deleteById(reviewId);
    }
}
