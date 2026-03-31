package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.dto.social.LikeResponseDto;

/**
 * Service for managing likes on reviews and game lists.
 */
public interface LikeService {

    /**
     * Toggles a like on a review. If the user already liked the review, the like is removed.
     * Otherwise, a new like is created.
     *
     * @param userEmail the authenticated user's email
     * @param reviewId  the review ID
     * @return a response indicating the new like status and updated count
     * @throws com.checkpoint.api.exceptions.ReviewNotFoundException if the review does not exist
     */
    LikeResponseDto toggleReviewLike(String userEmail, UUID reviewId);

    /**
     * Toggles a like on a game list. If the user already liked the list, the like is removed.
     * Otherwise, a new like is created.
     *
     * @param userEmail the authenticated user's email
     * @param listId    the game list ID
     * @return a response indicating the new like status and updated count
     * @throws com.checkpoint.api.exceptions.GameListNotFoundException if the game list does not exist
     */
    LikeResponseDto toggleListLike(String userEmail, UUID listId);
}
