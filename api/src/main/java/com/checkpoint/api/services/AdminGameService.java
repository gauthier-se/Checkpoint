package com.checkpoint.api.services;

import java.util.List;

import com.checkpoint.api.dto.admin.ExternalGameDto;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.ExternalApiUnavailableException;
import com.checkpoint.api.exceptions.ExternalGameNotFoundException;

/**
 * Service interface for admin game management operations.
 * Provides search and import functionality for external game APIs.
 */
public interface AdminGameService {

    /**
     * Searches for games on the external API (IGDB).
     * Returns lightweight DTOs for display before import.
     *
     * @param query the search term
     * @param limit maximum number of results (default 20)
     * @return list of matching external games
     */
    List<ExternalGameDto> searchExternalGames(String query, int limit);

    /**
     * Imports a single game by its external ID.
     * Fetches full details from the external API and saves to the database.
     *
     * @param externalId the IGDB game ID
     * @return the saved VideoGame entity
     * @throws ExternalGameNotFoundException if the game is not found on IGDB
     * @throws ExternalApiUnavailableException if IGDB API is unreachable
     */
    VideoGame importGameByExternalId(Long externalId);
}
