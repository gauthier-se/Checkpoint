package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.collection.WishResponseDto;

/**
 * Service for managing a user's wishlist.
 */
public interface WishlistService {

    /**
     * Adds a game to the authenticated user's wishlist.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID to add
     * @return the created wish entry
     */
    WishResponseDto addToWishlist(String userEmail, UUID videoGameId);

    /**
     * Removes a game from the authenticated user's wishlist.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID to remove
     */
    void removeFromWishlist(String userEmail, UUID videoGameId);

    /**
     * Returns the authenticated user's wishlist (paginated).
     *
     * @param userEmail the authenticated user's email
     * @param pageable  pagination parameters
     * @return paginated list of wished games
     */
    Page<WishResponseDto> getUserWishlist(String userEmail, Pageable pageable);

    /**
     * Checks if a game is in the authenticated user's wishlist.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID to check
     * @return true if the game is in the wishlist
     */
    boolean isInWishlist(String userEmail, UUID videoGameId);
}
