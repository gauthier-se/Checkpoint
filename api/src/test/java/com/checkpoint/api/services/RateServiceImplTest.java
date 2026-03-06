package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.dto.catalog.RateResponseDto;
import com.checkpoint.api.entities.Rate;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.RateNotFoundException;
import com.checkpoint.api.mapper.RateMapper;
import com.checkpoint.api.repositories.RateRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.impl.RateServiceImpl;

@ExtendWith(MockitoExtension.class)
class RateServiceImplTest {

    @Mock
    private RateRepository rateRepository;

    @Mock
    private VideoGameRepository videoGameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RateMapper rateMapper;

    private RateServiceImpl rateService;

    private User testUser;
    private VideoGame testGame;
    private UUID gameId;

    @BeforeEach
    void setUp() {
        rateService = new RateServiceImpl(rateRepository, videoGameRepository, userRepository, rateMapper);

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
    @DisplayName("rateGame()")
    class RateGame {

        @Test
        @DisplayName("Should create a new rating if none exists")
        void rateGame_shouldCreateNewRating() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
            when(rateRepository.findByUserEmailAndVideoGameId(testUser.getEmail(), gameId)).thenReturn(Optional.empty());

            Rate savedRate = new Rate(testUser, testGame, 4);
            savedRate.setId(UUID.randomUUID());
            when(rateRepository.save(any(Rate.class))).thenReturn(savedRate);
            when(rateRepository.calculateAverageRating(gameId)).thenReturn(4.0);

            RateResponseDto responseDto = new RateResponseDto(savedRate.getId(), 4, gameId, null, null);
            when(rateMapper.toDto(savedRate)).thenReturn(responseDto);

            // When
            RateResponseDto result = rateService.rateGame(testUser.getEmail(), gameId, 4);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.score()).isEqualTo(4);
            assertThat(result.videoGameId()).isEqualTo(gameId);
            verify(rateRepository).save(any(Rate.class));
            verify(videoGameRepository).save(testGame);
            assertThat(testGame.getAverageRating()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("Should update an existing rating")
        void rateGame_shouldUpdateExistingRating() {
            // Given
            Rate existingRate = new Rate(testUser, testGame, 3);
            existingRate.setId(UUID.randomUUID());

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
            when(rateRepository.findByUserEmailAndVideoGameId(testUser.getEmail(), gameId)).thenReturn(Optional.of(existingRate));

            when(rateRepository.save(existingRate)).thenReturn(existingRate);
            when(rateRepository.calculateAverageRating(gameId)).thenReturn(5.0);

            RateResponseDto responseDto = new RateResponseDto(existingRate.getId(), 5, gameId, null, null);
            when(rateMapper.toDto(existingRate)).thenReturn(responseDto);

            // When
            RateResponseDto result = rateService.rateGame(testUser.getEmail(), gameId, 5);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.score()).isEqualTo(5);
            assertThat(existingRate.getScore()).isEqualTo(5);
            verify(rateRepository).save(existingRate);
        }

        @Test
        @DisplayName("Should throw GameNotFoundException when game does not exist")
        void rateGame_shouldThrowWhenGameNotFound() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(gameId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rateService.rateGame(testUser.getEmail(), gameId, 4))
                    .isInstanceOf(GameNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user not found")
        void rateGame_shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rateService.rateGame("unknown@test.com", gameId, 4))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("removeRating()")
    class RemoveRating {

        @Test
        @DisplayName("Should remove an existing rating and recalculate average")
        void removeRating_shouldDeleteRating() {
            // Given
            Rate existingRate = new Rate(testUser, testGame, 4);
            existingRate.setId(UUID.randomUUID());

            when(rateRepository.findByUserEmailAndVideoGameId(testUser.getEmail(), gameId))
                    .thenReturn(Optional.of(existingRate));
            when(rateRepository.calculateAverageRating(gameId)).thenReturn(null);

            // When
            rateService.removeRating(testUser.getEmail(), gameId);

            // Then
            verify(rateRepository).delete(existingRate);
            verify(videoGameRepository).save(testGame);
            assertThat(testGame.getAverageRating()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should throw RateNotFoundException when no rating exists")
        void removeRating_shouldThrowWhenNoRatingExists() {
            // Given
            when(rateRepository.findByUserEmailAndVideoGameId(testUser.getEmail(), gameId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rateService.removeRating(testUser.getEmail(), gameId))
                    .isInstanceOf(RateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getUserRating()")
    class GetUserRating {

        @Test
        @DisplayName("Should return rating when it exists")
        void getUserRating_shouldReturnRating() {
            // Given
            Rate rate = new Rate(testUser, testGame, 4);
            rate.setId(UUID.randomUUID());

            when(rateRepository.findByUserEmailAndVideoGameId(testUser.getEmail(), gameId))
                    .thenReturn(Optional.of(rate));

            RateResponseDto responseDto = new RateResponseDto(rate.getId(), 4, gameId, null, null);
            when(rateMapper.toDto(rate)).thenReturn(responseDto);

            // When
            RateResponseDto result = rateService.getUserRating(testUser.getEmail(), gameId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.score()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should return null when no rating exists")
        void getUserRating_shouldReturnNullWhenNoRatingExists() {
            // Given
            when(rateRepository.findByUserEmailAndVideoGameId(testUser.getEmail(), gameId))
                    .thenReturn(Optional.empty());
            when(rateMapper.toDto(null)).thenReturn(null);

            // When
            RateResponseDto result = rateService.getUserRating(testUser.getEmail(), gameId);

            // Then
            assertThat(result).isNull();
        }
    }
}
