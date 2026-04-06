package com.checkpoint.api.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.GameCardDto;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.GameTrendingService;

/**
 * Implementation of {@link GameTrendingService}.
 * Computes trending games based on weighted recent user activity over a 7-day window.
 */
@Service
@Transactional(readOnly = true)
public class GameTrendingServiceImpl implements GameTrendingService {

    private static final Logger log = LoggerFactory.getLogger(GameTrendingServiceImpl.class);

    private static final int TRENDING_WINDOW_DAYS = 7;
    private static final int DEFAULT_SIZE = 7;
    private static final int MAX_SIZE = 20;

    private final VideoGameRepository videoGameRepository;

    public GameTrendingServiceImpl(VideoGameRepository videoGameRepository) {
        this.videoGameRepository = videoGameRepository;
    }

    @Override
    public List<GameCardDto> getTrendingGames(int size) {
        int validatedSize = Math.min(Math.max(1, size), MAX_SIZE);
        LocalDateTime since = LocalDateTime.now().minusDays(TRENDING_WINDOW_DAYS);

        log.debug("Fetching trending games - size: {}, since: {}", validatedSize, since);

        List<Object[]> results = videoGameRepository.findTrendingGames(since, validatedSize);

        return results.stream()
                .map(this::mapToGameCardDto)
                .toList();
    }

    /**
     * Maps a native SQL result row to a GameCardDto.
     *
     * @param row the result row (id, title, coverUrl, releaseDate, averageRating, ratingCount)
     * @return the mapped GameCardDto
     */
    private GameCardDto mapToGameCardDto(Object[] row) {
        UUID id = (UUID) row[0];
        String title = (String) row[1];
        String coverUrl = (String) row[2];
        LocalDate releaseDate = row[3] != null ? ((java.sql.Date) row[3]).toLocalDate() : null;
        Double averageRating = row[4] != null ? ((Number) row[4]).doubleValue() : null;
        Long ratingCount = row[5] != null ? ((Number) row[5]).longValue() : 0L;

        return new GameCardDto(id, title, coverUrl, releaseDate, averageRating, ratingCount);
    }
}
