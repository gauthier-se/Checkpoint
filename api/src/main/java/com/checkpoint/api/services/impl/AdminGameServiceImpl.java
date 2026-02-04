package com.checkpoint.api.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.client.IgdbApiClient;
import com.checkpoint.api.dto.admin.ExternalGameDto;
import com.checkpoint.api.dto.igdb.IgdbGameDto;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.ExternalApiUnavailableException;
import com.checkpoint.api.exceptions.ExternalGameNotFoundException;
import com.checkpoint.api.exceptions.IgdbApiException;
import com.checkpoint.api.services.AdminGameService;
import com.checkpoint.api.services.GameImportService;

/**
 * Implementation of {@link AdminGameService}.
 * Provides admin operations for searching and importing games from IGDB.
 */
@Service
@Transactional
public class AdminGameServiceImpl implements AdminGameService {

    private static final Logger log = LoggerFactory.getLogger(AdminGameServiceImpl.class);

    private final IgdbApiClient igdbApiClient;
    private final GameImportService gameImportService;

    public AdminGameServiceImpl(IgdbApiClient igdbApiClient, GameImportService gameImportService) {
        this.igdbApiClient = igdbApiClient;
        this.gameImportService = gameImportService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalGameDto> searchExternalGames(String query, int limit) {
        log.info("Searching external games with query: '{}', limit: {}", query, limit);

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        try {
            List<IgdbGameDto> igdbGames = igdbApiClient.searchGames(query, limit);

            return igdbGames.stream()
                    .map(this::mapToExternalGameDto)
                    .toList();

        } catch (IgdbApiException e) {
            log.error("IGDB API error during search: {}", e.getMessage(), e);
            throw new ExternalApiUnavailableException("External game API is currently unavailable", e);
        }
    }

    @Override
    public VideoGame importGameByExternalId(Long externalId) {
        log.info("Importing game with external ID: {}", externalId);

        if (externalId == null || externalId <= 0) {
            throw new IllegalArgumentException("External ID must be a positive number");
        }

        try {
            // Fetch the game from IGDB
            List<IgdbGameDto> games = igdbApiClient.fetchGamesByIds(List.of(externalId));

            if (games.isEmpty()) {
                log.warn("Game not found on IGDB with ID: {}", externalId);
                throw new ExternalGameNotFoundException(externalId);
            }

            // Import the game using existing service
            List<VideoGame> importedGames = gameImportService.importGamesByIds(List.of(externalId));

            if (importedGames.isEmpty()) {
                log.error("Failed to import game with external ID: {}", externalId);
                throw new ExternalApiUnavailableException("Failed to import game from external API");
            }

            VideoGame importedGame = importedGames.get(0);
            log.info("Successfully imported game: {} (ID: {})", importedGame.getTitle(), importedGame.getId());

            return importedGame;

        } catch (IgdbApiException e) {
            log.error("IGDB API error during import: {}", e.getMessage(), e);
            throw new ExternalApiUnavailableException("External game API is currently unavailable", e);
        }
    }

    /**
     * Maps an IGDB game DTO to a lightweight ExternalGameDto.
     */
    private ExternalGameDto mapToExternalGameDto(IgdbGameDto igdbGame) {
        String coverUrl = null;
        if (igdbGame.cover() != null) {
            coverUrl = igdbGame.cover().getCoverBigUrl();
        }

        return ExternalGameDto.fromIgdb(
                igdbGame.id(),
                igdbGame.name(),
                igdbGame.firstReleaseDate(),
                coverUrl
        );
    }
}
