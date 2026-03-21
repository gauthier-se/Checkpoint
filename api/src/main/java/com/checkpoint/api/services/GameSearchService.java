package com.checkpoint.api.services;

import java.util.List;

import com.checkpoint.api.dto.catalog.GameCardDto;

/**
 * Service interface for full-text game search using Hibernate Search.
 * Provides fuzzy search on game title and description with optional genre/platform filtering.
 */
public interface GameSearchService {

    /**
     * Searches for games matching the given query with optional filters.
     *
     * @param query    the search text (fuzzy matching on title and description)
     * @param genre    optional genre name filter
     * @param platform optional platform name filter
     * @return list of matching games sorted by relevance score
     */
    List<GameCardDto> searchGames(String query, String genre, String platform);
}
