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

import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.services.ReviewService;

/**
 * REST controller for public game reviews endpoints.
 *
 * <p>Provides read-only access to reviews for a specific game.
 * Reviews are now created, updated, and deleted via play log endpoints
 * ({@link PlayLogReviewController}).</p>
 */
@RestController
@RequestMapping("/api/games/{gameId}/reviews")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "createdAt,desc";

    private final ReviewService reviewService;

    /**
     * Constructs a new ReviewController.
     *
     * @param reviewService the review service
     */
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Retrieves a paginated list of all reviews for a specific game.
     * Includes reviews from all users and all their play logs, ordered by date.
     * Accessible to both public and authenticated users.
     *
     * @param gameId the video game ID
     * @param page   the page number (0-based)
     * @param size   the page size
     * @param sort   the sorting parameters
     * @return the paginated reviews with play context
     */
    @GetMapping
    public ResponseEntity<PagedResponseDto<ReviewResponseDto>> getReviews(
            @PathVariable UUID gameId,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

        log.info("GET /api/games/{}/reviews - page: {}, size: {}, sort: {}", gameId, page, size, sort);

        int validatedSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validatedPage = Math.max(0, page);

        Pageable pageable = createPageable(validatedPage, validatedSize, sort);
        Page<ReviewResponseDto> reviewPage = reviewService.getGameReviews(gameId, pageable);

        return ResponseEntity.ok(PagedResponseDto.from(reviewPage));
    }

    /**
     * Creates a Pageable from the sort string.
     * Supports format: "field,direction" (e.g., "createdAt,desc").
     *
     * @param page the page number
     * @param size the page size
     * @param sort the sort string
     * @return a Pageable instance
     */
    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0].trim();
        Sort.Direction direction = sortParts.length > 1
                && sortParts[1].trim().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String mappedField = mapSortField(sortField);

        return PageRequest.of(page, size, Sort.by(direction, mappedField));
    }

    /**
     * Maps API sort field names to entity field names.
     *
     * @param field the API field name
     * @return the entity field name
     */
    private String mapSortField(String field) {
        return switch (field.toLowerCase()) {
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            default -> "createdAt";
        };
    }
}
