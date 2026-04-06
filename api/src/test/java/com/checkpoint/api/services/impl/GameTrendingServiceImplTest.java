package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.dto.catalog.GameCardDto;
import com.checkpoint.api.repositories.VideoGameRepository;

/**
 * Unit tests for {@link GameTrendingServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class GameTrendingServiceImplTest {

    @Mock
    private VideoGameRepository videoGameRepository;

    private GameTrendingServiceImpl gameTrendingService;

    @BeforeEach
    void setUp() {
        gameTrendingService = new GameTrendingServiceImpl(videoGameRepository);
    }

    @Test
    @DisplayName("getTrendingGames should return mapped game cards from repository results")
    void getTrendingGames_shouldReturnMappedGameCards() {
        // Given
        UUID gameId = UUID.randomUUID();
        Object[] row = new Object[]{
                gameId,
                "Elden Ring",
                "cover.jpg",
                java.sql.Date.valueOf(LocalDate.of(2022, 2, 25)),
                4.9,
                2000L
        };
        List<Object[]> rows = new ArrayList<>();
        rows.add(row);
        when(videoGameRepository.findTrendingGames(argThat(since ->
                since.isAfter(LocalDateTime.now().minusDays(8))
                        && since.isBefore(LocalDateTime.now().minusDays(6))),
                anyInt()))
                .thenReturn(rows);

        // When
        List<GameCardDto> results = gameTrendingService.getTrendingGames(7);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(gameId);
        assertThat(results.get(0).title()).isEqualTo("Elden Ring");
        assertThat(results.get(0).coverUrl()).isEqualTo("cover.jpg");
        assertThat(results.get(0).releaseDate()).isEqualTo(LocalDate.of(2022, 2, 25));
        assertThat(results.get(0).averageRating()).isEqualTo(4.9);
        assertThat(results.get(0).ratingCount()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("getTrendingGames should use a 7-day window for trending calculation")
    void getTrendingGames_shouldUseSevenDayWindow() {
        // Given
        when(videoGameRepository.findTrendingGames(argThat(since ->
                since.isAfter(LocalDateTime.now().minusDays(8))
                        && since.isBefore(LocalDateTime.now().minusDays(6))),
                anyInt()))
                .thenReturn(List.of());

        // When
        gameTrendingService.getTrendingGames(7);

        // Then
        verify(videoGameRepository).findTrendingGames(
                argThat(since -> since.isAfter(LocalDateTime.now().minusDays(8))
                        && since.isBefore(LocalDateTime.now().minusDays(6))),
                anyInt());
    }

    @Test
    @DisplayName("getTrendingGames should cap size to maximum 20")
    void getTrendingGames_shouldCapSizeToMaximum() {
        // Given
        when(videoGameRepository.findTrendingGames(argThat(since ->
                since.isAfter(LocalDateTime.now().minusDays(8))),
                anyInt()))
                .thenReturn(List.of());

        // When
        gameTrendingService.getTrendingGames(50);

        // Then
        verify(videoGameRepository).findTrendingGames(
                argThat(since -> since.isAfter(LocalDateTime.now().minusDays(8))),
                org.mockito.ArgumentMatchers.eq(20));
    }

    @Test
    @DisplayName("getTrendingGames should return empty list when no games found")
    void getTrendingGames_shouldReturnEmptyListWhenNoGames() {
        // Given
        when(videoGameRepository.findTrendingGames(argThat(since ->
                since.isAfter(LocalDateTime.now().minusDays(8))),
                anyInt()))
                .thenReturn(List.of());

        // When
        List<GameCardDto> results = gameTrendingService.getTrendingGames(7);

        // Then
        assertThat(results).isEmpty();
    }
}
