package com.checkpoint.api.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.social.LikeResponseDto;
import com.checkpoint.api.services.LikeService;

/**
 * REST controller for like/unlike toggle on reviews and game lists.
 * All endpoints require authentication.
 */
@RestController
public class LikeController {

    private static final Logger log = LoggerFactory.getLogger(LikeController.class);

    private final LikeService likeService;

    /**
     * Constructs a new LikeController.
     *
     * @param likeService the like service
     */
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    /**
     * Toggles a like on a review. If the user already liked it, the like is removed.
     * Otherwise, a new like is created.
     *
     * @param userDetails the authenticated user principal
     * @param reviewId    the review ID
     * @return the new like status and updated count
     */
    @PostMapping("/api/reviews/{reviewId}/like")
    public ResponseEntity<LikeResponseDto> toggleReviewLike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID reviewId) {

        log.info("POST /api/reviews/{}/like - user: {}", reviewId, userDetails.getUsername());

        LikeResponseDto response = likeService.toggleReviewLike(
                userDetails.getUsername(), reviewId);

        return ResponseEntity.ok(response);
    }

    /**
     * Toggles a like on a game list. If the user already liked it, the like is removed.
     * Otherwise, a new like is created.
     *
     * @param userDetails the authenticated user principal
     * @param listId      the game list ID
     * @return the new like status and updated count
     */
    @PostMapping("/api/lists/{listId}/like")
    public ResponseEntity<LikeResponseDto> toggleListLike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID listId) {

        log.info("POST /api/lists/{}/like - user: {}", listId, userDetails.getUsername());

        LikeResponseDto response = likeService.toggleListLike(
                userDetails.getUsername(), listId);

        return ResponseEntity.ok(response);
    }
}
