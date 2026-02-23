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
import org.mockito.InjectMocks;
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
import com.checkpoint.api.repositories.RateRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.impl.ReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RateRepository rateRepository;

    @Mock
    private VideoGameRepository videoGameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private VideoGame testGame;
    private UUID gameId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setPseudo("testuser");
        testUser.setEmail("test@test.com");

        testGame = new VideoGame();
        testGame.setId(gameId);
        testGame.setTitle("Test Game");
        testGame.setAverageRating(0.0);
    }

    @Nested
    @DisplayName("addOrUpdateReview()")
    class AddOrUpdateReview {

        @Test
        @DisplayName("Should create a new review if none exists")
        void shouldCreateNewReview() {
            // Given
            ReviewRequestDto requestDto = new ReviewRequestDto(5, "Great game!", false);
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
            when(reviewRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId)).thenReturn(Optional.empty());
            when(rateRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId)).thenReturn(Optional.empty());

            com.checkpoint.api.entities.Rate rate = new com.checkpoint.api.entities.Rate(testUser, testGame, 5);
            when(rateRepository.save(any(com.checkpoint.api.entities.Rate.class))).thenReturn(rate);

            Review savedReview = new Review("Great game!", false, testUser, testGame);
            savedReview.setId(UUID.randomUUID());
            when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

            when(rateRepository.calculateAverageRating(gameId)).thenReturn(5.0);

            ReviewResponseDto responseDto = new ReviewResponseDto(savedReview.getId(), 5, "Great game!", false, null, null, null);
            when(reviewMapper.toDto(savedReview, 5)).thenReturn(responseDto);

            // When
            ReviewResponseDto result = reviewService.addOrUpdateReview(testUser.getEmail(), gameId, requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.score()).isEqualTo(5);
            verify(reviewRepository).save(any(Review.class));
            verify(rateRepository).save(any(com.checkpoint.api.entities.Rate.class));
            verify(videoGameRepository).save(testGame);
            assertThat(testGame.getAverageRating()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Should update an existing review if one exists")
        void shouldUpdateExistingReview() {
            // Given
            Review existingReview = new Review("Okay game", false, testUser, testGame);
            existingReview.setId(UUID.randomUUID());
            com.checkpoint.api.entities.Rate existingRate = new com.checkpoint.api.entities.Rate(testUser, testGame, 3);
            existingRate.setId(UUID.randomUUID());

            ReviewRequestDto requestDto = new ReviewRequestDto(4, "Actually, it's better", true);
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
            when(reviewRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId)).thenReturn(Optional.of(existingReview));
            when(rateRepository.findByUserPseudoAndVideoGameId(testUser.getPseudo(), gameId)).thenReturn(Optional.of(existingRate));

            when(reviewRepository.save(existingReview)).thenReturn(existingReview);
            when(rateRepository.save(existingRate)).thenReturn(existingRate);

            when(rateRepository.calculateAverageRating(gameId)).thenReturn(4.0);

            ReviewResponseDto responseDto = new ReviewResponseDto(existingReview.getId(), 4, "Actually, it's better", true, null, null, null);
            when(reviewMapper.toDto(existingReview, 4)).thenReturn(responseDto);

            // When
            ReviewResponseDto result = reviewService.addOrUpdateReview(testUser.getEmail(), gameId, requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.score()).isEqualTo(4);
            assertThat(existingReview.getContent()).isEqualTo("Actually, it's better");
            verify(reviewRepository).save(existingReview);
            verify(rateRepository).save(existingRate);
            assertThat(testGame.getAverageRating()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("Should throw GameNotFoundException when video game does not exist")
        void shouldThrowExceptionWhenGameNotFound() {
            // Given
            ReviewRequestDto requestDto = new ReviewRequestDto(5, "Great game!", false);
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
            com.checkpoint.api.entities.Rate rate = new com.checkpoint.api.entities.Rate(testUser, testGame, 4);

            when(videoGameRepository.existsById(gameId)).thenReturn(true);
            when(reviewRepository.findByVideoGameId(gameId, pageable)).thenReturn(reviewPage);
            when(rateRepository.findByVideoGameIdAndUserIdIn(gameId, java.util.List.of(testUser.getId()))).thenReturn(java.util.List.of(rate));

            ReviewResponseDto responseDto = new ReviewResponseDto(UUID.randomUUID(), 4, "Good", false, null, null, null);
            when(reviewMapper.toDto(review, 4)).thenReturn(responseDto);

            // When
            Page<ReviewResponseDto> result = reviewService.getGameReviews(gameId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).score()).isEqualTo(4);
        }
    }
}
