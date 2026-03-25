package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

/**
 * Unit tests for {@link ReportServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    private ReportServiceImpl reportService;

    private User user;
    private Review review;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(reportRepository, reviewRepository, userRepository);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPseudo("testuser");

        reviewId = UUID.randomUUID();
        review = new Review();
        review.setId(reviewId);
    }

    @Nested
    @DisplayName("reportReview")
    class ReportReview {

        @Test
        @DisplayName("should create report and return response")
        void reportReview_shouldCreateReport() {
            // Given
            ReportRequestDto request = new ReportRequestDto("Inappropriate content");
            Report savedReport = Report.onReview("Inappropriate content", user, review);
            savedReport.setId(UUID.randomUUID());
            savedReport.setCreatedAt(LocalDateTime.now());

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reportRepository.existsByUserIdAndReviewId(user.getId(), reviewId)).thenReturn(false);
            when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

            // When
            ReportResponseDto result = reportService.reportReview("user@example.com", reviewId, request);

            // Then
            assertThat(result.id()).isEqualTo(savedReport.getId());
            assertThat(result.content()).isEqualTo("Inappropriate content");
            assertThat(result.createdAt()).isEqualTo(savedReport.getCreatedAt());
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("should throw ReviewNotFoundException when review does not exist")
        void reportReview_shouldThrowWhenReviewNotFound() {
            // Given
            ReportRequestDto request = new ReportRequestDto("Inappropriate content");
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> reportService.reportReview("user@example.com", reviewId, request))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining(reviewId.toString());
        }

        @Test
        @DisplayName("should throw DuplicateReportException when user already reported")
        void reportReview_shouldThrowWhenAlreadyReported() {
            // Given
            ReportRequestDto request = new ReportRequestDto("Inappropriate content");
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reportRepository.existsByUserIdAndReviewId(user.getId(), reviewId)).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> reportService.reportReview("user@example.com", reviewId, request))
                    .isInstanceOf(DuplicateReportException.class)
                    .hasMessageContaining(reviewId.toString());
        }
    }
}
