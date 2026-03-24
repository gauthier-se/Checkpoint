package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.entities.Report;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.services.impl.AdminReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    private AdminReviewServiceImpl adminReviewService;

    @BeforeEach
    void setUp() {
        adminReviewService = new AdminReviewServiceImpl(reviewRepository);
    }

    private User createUser(String pseudo) {
        User user = new User();
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
        assertThat(dto.authorUsername()).isEqualTo("bob");
        assertThat(dto.gameTitle()).isEqualTo("Game X");
        assertThat(dto.reportCount()).isEqualTo(2);

        assertThat(result.metadata().totalElements()).isEqualTo(1);
        verify(reviewRepository).findByReportsIsNotEmpty(pageable);
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
