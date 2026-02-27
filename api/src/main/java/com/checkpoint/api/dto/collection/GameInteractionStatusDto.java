package com.checkpoint.api.dto.collection;

import com.checkpoint.api.enums.GameStatus;

/**
 * DTO representing a user's aggregate interaction state with a specific game.
 */
public record GameInteractionStatusDto(
        boolean inWishlist,
        boolean inBacklog,
        boolean inLibrary,
        GameStatus libraryStatus,
        int playCount,
        Integer userRating,
        boolean hasReview
) {
}
