package com.checkpoint.api.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.dto.list.GameListCardDto;
import com.checkpoint.api.dto.list.GameListDetailDto;
import com.checkpoint.api.services.GameListService;

/**
 * REST controller for public game list browsing.
 * All endpoints are publicly accessible. Optional authentication provides
 * additional context (isOwner, hasLiked) on detail view.
 */
@RestController
@RequestMapping("/api/lists")
public class GameListController {

    private static final Logger log = LoggerFactory.getLogger(GameListController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final GameListService gameListService;

    /**
     * Constructs a new GameListController.
     *
     * @param gameListService the game list service
     */
    public GameListController(GameListService gameListService) {
        this.gameListService = gameListService;
    }

    /**
     * Returns a paginated list of recent public game lists.
     *
     * @param page the page number (0-based, default 0)
     * @param size the page size (default 20, max 100)
     * @return paginated list of game list card DTOs
     */
    @GetMapping
    public ResponseEntity<PagedResponseDto<GameListCardDto>> getRecentLists(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size) {

        log.info("GET /api/lists - page: {}, size: {}", page, size);

        int validatedSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validatedPage = Math.max(0, page);

        Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GameListCardDto> lists = gameListService.getRecentPublicLists(pageable);

        return ResponseEntity.ok(PagedResponseDto.from(lists));
    }

    /**
     * Returns a paginated list of popular public game lists (sorted by like count).
     *
     * @param page the page number (0-based, default 0)
     * @param size the page size (default 20, max 100)
     * @return paginated list of game list card DTOs
     */
    @GetMapping("/popular")
    public ResponseEntity<PagedResponseDto<GameListCardDto>> getPopularLists(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size) {

        log.info("GET /api/lists/popular - page: {}, size: {}", page, size);

        int validatedSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validatedPage = Math.max(0, page);

        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        Page<GameListCardDto> lists = gameListService.getPopularPublicLists(pageable);

        return ResponseEntity.ok(PagedResponseDto.from(lists));
    }

    /**
     * Returns the detail of a game list by ID.
     * Private lists are only accessible to the owner (requires authentication).
     *
     * @param listId      the list ID
     * @param userDetails the authenticated user, or null if anonymous
     * @return the game list detail DTO
     */
    @GetMapping("/{listId}")
    public ResponseEntity<GameListDetailDto> getListDetail(
            @PathVariable UUID listId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/lists/{} - viewer: {}", listId,
                userDetails != null ? userDetails.getUsername() : "anonymous");

        String viewerEmail = userDetails != null ? userDetails.getUsername() : null;
        GameListDetailDto detail = gameListService.getListDetail(listId, viewerEmail);

        return ResponseEntity.ok(detail);
    }
}
