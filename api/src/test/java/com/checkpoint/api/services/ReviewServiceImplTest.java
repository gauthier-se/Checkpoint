package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.mapper.ReviewMapper;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.impl.ReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private VideoGameRepository videoGameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    private ReviewServiceImpl reviewService;

    private User testUser;
    private VideoGame testGame;
    private UUID gameId;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, videoGameRepository, userRepository, reviewMapper);

        gameId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setPseudo("testuser");
        testUser.setEmail("test@test.com");

        testGame = new VideoGame();
        testGame.setId(gameId);
        testGame.setTitle("Test Game");
    }

    @Nested
    @DisplayName("addOrUpdateReview()")
    class AddOrUpdateReview {

        @Test
        @DisplayName("Should create a new review if none exists")
        void shouldCreateNewReview() {
            // Given
            ReviewRequestDto requestDto = new ReviewRequestDto("Great game!", false);
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
            when(reviewRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId)).thenReturn(Optional.empty());

            Review savedReview = new Review("Great game!", false, testUser, testGame);
            savedReview.setId(UUID.randomUUID());
            when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

            ReviewResponseDto responseDto = new ReviewResponseDto(savedReview.getId(), "Great game!", false, null, null, null);
            when(reviewMapper.toDto(savedReview)).thenReturn(responseDto);

            // When
            ReviewResponseDto result = reviewService.addOrUpdateReview(testUser.getEmail(), gameId, requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Great game!");
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("Should update an existing review if one exists")
        void shouldUpdateExistingReview() {
            // Given
            Review existingReview = new Review("Okay game", false, testUser, testGame);
            existingReview.setId(UUID.randomUUID());

            ReviewRequestDto requestDto = new ReviewRequestDto("Actually, it's better", true);
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
            when(reviewRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId)).thenReturn(Optional.of(existingReview));

            when(reviewRepository.save(existingReview)).thenReturn(existingReview);

            ReviewResponseDto responseDto = new ReviewResponseDto(existingReview.getId(), "Actually, it's better", true, null, null, null);
            when(reviewMapper.toDto(existingReview)).thenReturn(responseDto);

            // When
            ReviewResponseDto result = reviewService.addOrUpdateReview(testUser.getEmail(), gameId, requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Actually, it's better");
            assertThat(existingReview.getContent()).isEqualTo("Actually, it's better");
            verify(reviewRepository).save(existingReview);
        }

        @Test
        @DisplayName("Should throw GameNotFoundException when video game does not exist")
        void shouldThrowExceptionWhenGameNotFound() {
            // Given
            ReviewRequestDto requestDto = new ReviewRequestDto("Great game!", false);
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(gameId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reviewService.addOrUpdateReview(testUser.getEmail(), gameId, requestDto))
                    .isInstanceOf(GameNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getGameReviews()")
    class GetGameReviews {

        @Test
        @DisplayName("Should return a paginated list of reviews")
        void shouldReturnPaginatedReviews() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Review review = new Review("Good", false, testUser, testGame);
            Page<Review> reviewPage = new PageImpl<>(List.of(review));

            when(videoGameRepository.existsById(gameId)).thenReturn(true);
            when(reviewRepository.findByVideoGameId(gameId, pageable)).thenReturn(reviewPage);

            ReviewResponseDto responseDto = new ReviewResponseDto(UUID.randomUUID(), "Good", false, null, null, null);
            when(reviewMapper.toDto(review)).thenReturn(responseDto);

            // When
            Page<ReviewResponseDto> result = reviewService.getGameReviews(gameId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).content()).isEqualTo("Good");
        }
    }

    @Nested
    @DisplayName("deleteReview()")
    class DeleteReview {

        @Test
        @DisplayName("Should delete a review without touching ratings")
        void shouldDeleteReview() {
            // Given
            Review existingReview = new Review("Good", false, testUser, testGame);
            existingReview.setId(UUID.randomUUID());

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(reviewRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId))
                    .thenReturn(Optional.of(existingReview));

            // When
            reviewService.deleteReview(testUser.getEmail(), gameId);

            // Then
            verify(reviewRepository).delete(existingReview);
        }
    }

    @Nested
    @DisplayName("getReviewByUserAndGame()")
    class GetReviewByUserAndGame {

        @Test
        @DisplayName("Should return review when it exists")
        void shouldReturnReviewWhenExists() {
            // Given
            Review review = new Review("Great game!", false, testUser, testGame);
            review.setId(UUID.randomUUID());

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.existsById(gameId)).thenReturn(true);
            when(reviewRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId))
                    .thenReturn(Optional.of(review));

            ReviewResponseDto responseDto = new ReviewResponseDto(review.getId(), "Great game!", false, null, null, null);
            when(reviewMapper.toDto(review)).thenReturn(responseDto);

            // When
            ReviewResponseDto result = reviewService.getReviewByUserAndGame(testUser.getEmail(), gameId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Great game!");
        }

        @Test
        @DisplayName("Should return null when no review exists")
        void shouldReturnNullWhenNoReviewExists() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.existsById(gameId)).thenReturn(true);
            when(reviewRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId))
                    .thenReturn(Optional.empty());

            // When
            ReviewResponseDto result = reviewService.getReviewByUserAndGame(testUser.getEmail(), gameId);

            // Then
            assertThat(result).isNull();
        }
    }
}
