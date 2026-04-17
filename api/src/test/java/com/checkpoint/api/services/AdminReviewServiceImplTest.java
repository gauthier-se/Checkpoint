package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.admin.AdminReportedReviewDto;
import com.checkpoint.api.dto.admin.AdminReviewDto;
import com.checkpoint.api.dto.admin.AdminReviewReportDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.entities.Report;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.repositories.ReportRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.services.impl.AdminReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReportRepository reportRepository;

    private AdminReviewServiceImpl adminReviewService;

    @BeforeEach
    void setUp() {
        adminReviewService = new AdminReviewServiceImpl(reviewRepository, reportRepository);
    }

    private User createUser(String pseudo) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPseudo(pseudo);
        return user;
    }

    private VideoGame createGame(String title) {
        VideoGame game = new VideoGame();
        game.setTitle(title);
        return game;
    }

    private Review createReview(UUID id, String content, Boolean spoilers, User user, VideoGame game) {
        Review review = new Review();
        review.setId(id);
        review.setContent(content);
        review.setHaveSpoilers(spoilers);
        review.setUser(user);
        review.setVideoGame(game);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    private Report createReport(UUID id, String content, User user) {
        Report report = new Report(content, user);
        report.setId(id);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    @Test
    @DisplayName("getAllReviews should return paginated list of mapped DTOs")
    void getAllReviews_shouldReturnPaginatedMappedDtos() {
        // Given
        UUID id1 = UUID.randomUUID();
        User user1 = createUser("alice");
        VideoGame game1 = createGame("Game 1");
        Review review1 = createReview(id1, "Great game!", false, user1, game1);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(review1), pageable, 1);
        when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);

        // When
        PagedResponseDto<AdminReviewDto> result = adminReviewService.getAllReviews(pageable);

        // Then
        assertThat(result.content()).hasSize(1);
        AdminReviewDto dto = result.content().getFirst();
        assertThat(dto.id()).isEqualTo(id1);
        assertThat(dto.content()).isEqualTo("Great game!");
        assertThat(dto.haveSpoilers()).isFalse();
        assertThat(dto.authorUsername()).isEqualTo("alice");
        assertThat(dto.gameTitle()).isEqualTo("Game 1");

        assertThat(result.metadata().totalElements()).isEqualTo(1);
        verify(reviewRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getReportedReviews should return paginated list of reported reviews with report counts")
    void getReportedReviews_shouldReturnPaginatedReportedReviews() {
        // Given
        UUID id1 = UUID.randomUUID();
        User user1 = createUser("bob");
        VideoGame game1 = createGame("Game X");
        Review review1 = createReview(id1, "Offensive content", false, user1, game1);

        Report report1 = new Report("Spam", user1);
        Report report2 = new Report("Inappropriate", user1);
        Set<Report> reports = new HashSet<>();
        reports.add(report1);
        reports.add(report2);
        review1.setReports(reports);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(review1), pageable, 1);
        when(reviewRepository.findByReportsIsNotEmpty(pageable)).thenReturn(reviewPage);

        // When
        PagedResponseDto<AdminReportedReviewDto> result = adminReviewService.getReportedReviews(pageable);

        // Then
        assertThat(result.content()).hasSize(1);
        AdminReportedReviewDto dto = result.content().getFirst();
        assertThat(dto.id()).isEqualTo(id1);
        assertThat(dto.content()).isEqualTo("Offensive content");
        assertThat(dto.authorId()).isEqualTo(user1.getId());
        assertThat(dto.authorUsername()).isEqualTo("bob");
        assertThat(dto.gameTitle()).isEqualTo("Game X");
        assertThat(dto.reportCount()).isEqualTo(2);

        assertThat(result.metadata().totalElements()).isEqualTo(1);
        verify(reviewRepository).findByReportsIsNotEmpty(pageable);
    }

    @Test
    @DisplayName("getReviewReports should return paginated list of reports mapped to DTOs")
    void getReviewReports_shouldReturnPaginatedMappedDtos() {
        // Given
        UUID reviewId = UUID.randomUUID();
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();
        User reporter1 = createUser("carol");
        User reporter2 = createUser("dave");
        Report report1 = createReport(reportId1, "Spam", reporter1);
        Report report2 = createReport(reportId2, "Harassment", reporter2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(List.of(report1, report2), pageable, 2);
        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(reportRepository.findByReviewId(reviewId, pageable)).thenReturn(reportPage);

        // When
        PagedResponseDto<AdminReviewReportDto> result = adminReviewService.getReviewReports(reviewId, pageable);

        // Then
        assertThat(result.content()).hasSize(2);
        AdminReviewReportDto dto1 = result.content().get(0);
        assertThat(dto1.id()).isEqualTo(reportId1);
        assertThat(dto1.reporterUsername()).isEqualTo("carol");
        assertThat(dto1.reason()).isEqualTo("Spam");
        assertThat(dto1.createdAt()).isNotNull();

        AdminReviewReportDto dto2 = result.content().get(1);
        assertThat(dto2.reporterUsername()).isEqualTo("dave");
        assertThat(dto2.reason()).isEqualTo("Harassment");

        assertThat(result.metadata().totalElements()).isEqualTo(2);
        verify(reviewRepository).existsById(reviewId);
        verify(reportRepository).findByReviewId(reviewId, pageable);
    }

    @Test
    @DisplayName("getReviewReports should throw ReviewNotFoundException when review does not exist")
    void getReviewReports_shouldThrowWhenReviewMissing() {
        // Given
        UUID reviewId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> adminReviewService.getReviewReports(reviewId, pageable))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining(reviewId.toString());

        verify(reviewRepository).existsById(reviewId);
        verifyNoInteractions(reportRepository);
    }

    @Test
    @DisplayName("deleteReview should delete when review exists")
    void deleteReview_shouldDeleteWhenReviewExists() {
        // Given
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.existsById(reviewId)).thenReturn(true);

        // When
        adminReviewService.deleteReview(reviewId);

        // Then
        verify(reviewRepository).existsById(reviewId);
        verify(reviewRepository).deleteById(reviewId);
    }

    @Test
    @DisplayName("deleteReview should throw exception when review does not exist")
    void deleteReview_shouldThrowExceptionWhenNotFound() {
        // Given
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> adminReviewService.deleteReview(reviewId));
        verify(reviewRepository).existsById(reviewId);
    }
}
