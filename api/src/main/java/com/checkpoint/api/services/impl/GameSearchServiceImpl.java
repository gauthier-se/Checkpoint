package com.checkpoint.api.services.impl;

import java.util.List;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.GameCardDto;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.GameSearchService;

import jakarta.persistence.EntityManager;

/**
 * Implementation of {@link GameSearchService} using Hibernate Search with Lucene backend.
 * Provides fuzzy full-text search on game title and description with optional filtering.
 */
@Service
@Transactional(readOnly = true)
public class GameSearchServiceImpl implements GameSearchService {

    private static final Logger log = LoggerFactory.getLogger(GameSearchServiceImpl.class);

    private static final int MAX_RESULTS = 50;
    private static final int FUZZY_MAX_EDIT_DISTANCE = 2;

    private final EntityManager entityManager;
    private final VideoGameRepository videoGameRepository;

    public GameSearchServiceImpl(EntityManager entityManager, VideoGameRepository videoGameRepository) {
        this.entityManager = entityManager;
        this.videoGameRepository = videoGameRepository;
    }

    @Override
    public List<GameCardDto> searchGames(String query, String genre, String platform) {
        log.debug("Searching games - query: '{}', genre: '{}', platform: '{}'", query, genre, platform);

        SearchSession searchSession = Search.session(entityManager);

        List<VideoGame> results = searchSession.search(VideoGame.class)
                .where((SearchPredicateFactory f) -> {
                    BooleanPredicateClausesStep<?> bool = f.bool()
                            .must(f.match()
                                    .fields("title", "description")
                                    .matching(query)
                                    .fuzzy(FUZZY_MAX_EDIT_DISTANCE));

                    if (genre != null && !genre.isBlank()) {
                        bool.filter(f.match()
                                .field("genres.name")
                                .matching(genre));
                    }

                    if (platform != null && !platform.isBlank()) {
                        bool.filter(f.match()
                                .field("platforms.name")
                                .matching(platform));
                    }

                    return bool;
                })
                .fetchHits(MAX_RESULTS);

        log.debug("Search returned {} results", results.size());

        return results.stream()
                .map(this::mapToGameCardDto)
                .toList();
    }

    /**
     * Maps a VideoGame entity to a GameCardDto.
     */
    private GameCardDto mapToGameCardDto(VideoGame game) {
        Long ratingCount = videoGameRepository.countRatings(game.getId());
        return new GameCardDto(
                game.getId(),
                game.getTitle(),
                game.getCoverUrl(),
                game.getReleaseDate(),
                game.getAverageRating(),
                ratingCount
        );
    }
}
