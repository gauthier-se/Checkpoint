package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.catalog.GameCardDto;
import com.checkpoint.api.dto.catalog.GameDetailDto;

/**
 * Service interface for game catalog operations.
 * Provides methods for retrieving games for public display.
 */
public interface GameCatalogService {

    /**
     * Retrieves a paginated list of games for the catalog.
     *
     * @param pageable pagination and sorting parameters
     * @return page of game cards
     */
    Page<GameCardDto> getGameCatalog(Pageable pageable);

    /**
     * Retrieves detailed information about a specific game.
     *
     * @param id the game ID
     * @return game details
     * @throws GameNotFoundException if the game is not found
     */
    GameDetailDto getGameDetails(UUID id);
}
