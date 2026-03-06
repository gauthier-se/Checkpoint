package com.checkpoint.api.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.services.ReviewService;

import jakarta.validation.Valid;

/**
 * REST controller for game reviews endpoints.
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

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Retrieves a paginated list of public reviews for a specific game.
     * Accessible to both public and authenticated users.
     *
     * @param gameId the video game ID
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sorting parameters
     * @return the paginated reviews
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
     * Submits or updates a review for a specific game.
     * Only accessible to authenticated users.
     *
     * @param userDetails the authenticated user principal
     * @param gameId the video game ID
     * @param request the review request body containing content and spoiler flag
     * @return the created or updated review
     */
    @PostMapping
    public ResponseEntity<ReviewResponseDto> submitOrUpdateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID gameId,
            @Valid @RequestBody ReviewRequestDto request) {

        log.info("POST /api/games/{}/reviews - user: {}", gameId, userDetails.getUsername());
        ReviewResponseDto response = reviewService.addOrUpdateReview(userDetails.getUsername(), gameId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes the authenticated user's review for a specific game.
     *
     * @param userDetails the authenticated user principal
     * @param gameId the video game ID
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID gameId) {

        log.info("DELETE /api/games/{}/reviews - user: {}", gameId, userDetails.getUsername());

        reviewService.deleteReview(userDetails.getUsername(), gameId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves the authenticated user's review for a specific game.
     *
     * @param userDetails the authenticated user principal
     * @param gameId the video game ID
     * @return the review if found, or 404 Not Found if no review is left by the user yet
     */
    @GetMapping("/me")
    public ResponseEntity<ReviewResponseDto> getMyReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID gameId) {

        log.info("GET /api/games/{}/reviews/me - user: {}", gameId, userDetails.getUsername());

        ReviewResponseDto response = reviewService.getReviewByUserAndGame(userDetails.getUsername(), gameId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a Pageable from the sort string.
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
     */
    private String mapSortField(String field) {
        return switch (field.toLowerCase()) {
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            default -> "createdAt";
        };
    }
}
