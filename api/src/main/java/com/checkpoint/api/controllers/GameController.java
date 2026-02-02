package com.checkpoint.api.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.catalog.GameCardDto;
import com.checkpoint.api.dto.catalog.GameDetailDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.services.GameCatalogService;

/**
 * REST controller for public game catalog endpoints.
 * Provides paginated access to the game catalog and game details.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "releaseDate,desc";

    private final GameCatalogService gameCatalogService;

    public GameController(GameCatalogService gameCatalogService) {
        this.gameCatalogService = gameCatalogService;
    }

    /**
     * Retrieves a paginated list of games.
     *
     * @param page the page number (0-based, default 0)
     * @param size the page size (default 20, max 100)
     * @param sort the sort criteria (e.g., "releaseDate,desc" or "title,asc")
     * @return paginated list of game cards
     */
    @GetMapping
    public ResponseEntity<PagedResponseDto<GameCardDto>> getGames(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

        log.info("GET /api/games - page: {}, size: {}, sort: {}", page, size, sort);

        // Validate and sanitize inputs
        int validatedSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validatedPage = Math.max(0, page);

        Pageable pageable = createPageable(validatedPage, validatedSize, sort);
        Page<GameCardDto> gamePage = gameCatalogService.getGameCatalog(pageable);

        return ResponseEntity.ok(PagedResponseDto.from(gamePage));
    }

    /**
     * Retrieves detailed information about a specific game.
     *
     * @param id the game ID
     * @return game details
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameDetailDto> getGameById(@PathVariable UUID id) {
        log.info("GET /api/games/{}", id);

        GameDetailDto game = gameCatalogService.getGameDetails(id);
        return ResponseEntity.ok(game);
    }

    /**
     * Creates a Pageable from the sort string.
     * Supports format: "field,direction" (e.g., "releaseDate,desc")
     */
    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0].trim();
        Sort.Direction direction = sortParts.length > 1
                && sortParts[1].trim().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Map common sort fields to entity fields
        String mappedField = mapSortField(sortField);

        return PageRequest.of(page, size, Sort.by(direction, mappedField));
    }

    /**
     * Maps API sort field names to entity field names.
     */
    private String mapSortField(String field) {
        return switch (field.toLowerCase()) {
            case "releasedate", "release_date" -> "releaseDate";
            case "title", "name" -> "title";
            case "rating" -> "averageRating"; // Note: requires special handling in query
            case "createdat", "created_at" -> "createdAt";
            default -> "releaseDate";
        };
    }
}
