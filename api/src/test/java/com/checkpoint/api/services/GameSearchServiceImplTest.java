package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.dto.catalog.GameCardDto;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.impl.GameSearchServiceImpl;

import jakarta.persistence.EntityManager;

/**
 * Unit tests for {@link GameSearchServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class GameSearchServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private VideoGameRepository videoGameRepository;

    private GameSearchServiceImpl gameSearchService;

    @BeforeEach
    void setUp() {
        gameSearchService = new GameSearchServiceImpl(entityManager, videoGameRepository);
    }

    /**
     * Creates a mocked SearchSession that returns the given results.
     * Uses raw types and doReturn to bypass Hibernate Search's complex generic fluent API.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private SearchSession mockSearchSession(List<VideoGame> results) {
        SearchSession searchSession = mock(SearchSession.class);
        Object selectStep = mock(org.hibernate.search.engine.search.query.dsl.SearchQuerySelectStep.class);
        Object optionsStep = mock(org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep.class);

        Mockito.doReturn(selectStep).when(searchSession).search(any(Class.class));
        Mockito.doReturn(optionsStep)
                .when((org.hibernate.search.engine.search.query.dsl.SearchQuerySelectStep) selectStep)
                .where(any(java.util.function.Function.class));
        Mockito.doReturn(results)
                .when((org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep) optionsStep)
                .fetchHits(anyInt());

        return searchSession;
    }

    @Test
    @DisplayName("searchGames should return mapped GameCardDto list")
    void searchGames_shouldReturnMappedResults() {
        // Given
        UUID gameId = UUID.randomUUID();
        VideoGame game = new VideoGame("The Witcher 3", "An epic RPG", LocalDate.of(2015, 5, 19));
        game.setId(gameId);
        game.setCoverUrl("cover.jpg");
        game.setAverageRating(4.8);

        when(videoGameRepository.countRatings(gameId)).thenReturn(1500L);

        SearchSession searchSession = mockSearchSession(List.of(game));

        try (MockedStatic<org.hibernate.search.mapper.orm.Search> searchStatic =
                     Mockito.mockStatic(org.hibernate.search.mapper.orm.Search.class)) {

            searchStatic.when(() -> org.hibernate.search.mapper.orm.Search.session(entityManager))
                    .thenReturn(searchSession);

            // When
            List<GameCardDto> results = gameSearchService.searchGames("Witcher", null, null);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).id()).isEqualTo(gameId);
            assertThat(results.get(0).title()).isEqualTo("The Witcher 3");
            assertThat(results.get(0).coverUrl()).isEqualTo("cover.jpg");
            assertThat(results.get(0).averageRating()).isEqualTo(4.8);
            assertThat(results.get(0).ratingCount()).isEqualTo(1500L);
        }
    }

    @Test
    @DisplayName("searchGames should return empty list when no matches")
    void searchGames_shouldReturnEmptyListWhenNoMatches() {
        // Given
        SearchSession searchSession = mockSearchSession(List.of());

        try (MockedStatic<org.hibernate.search.mapper.orm.Search> searchStatic =
                     Mockito.mockStatic(org.hibernate.search.mapper.orm.Search.class)) {

            searchStatic.when(() -> org.hibernate.search.mapper.orm.Search.session(entityManager))
                    .thenReturn(searchSession);

            // When
            List<GameCardDto> results = gameSearchService.searchGames("nonexistent", null, null);

            // Then
            assertThat(results).isEmpty();
        }
    }
}
