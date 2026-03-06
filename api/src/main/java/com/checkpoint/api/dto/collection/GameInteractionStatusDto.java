package com.checkpoint.api.dto.collection;

import com.checkpoint.api.enums.GameStatus;

/**
 * DTO representing a user's aggregate interaction state with a specific game.
 *
 * @param inWishlist     whether the game is in the user's wishlist
 * @param inBacklog      whether the game is in the user's backlog
 * @param inLibrary      whether the game is in the user's library
 * @param libraryStatus  the game's status in the library
 * @param playCount      the number of play sessions
 * @param userRating     the user's global rating from the Rate entity
 * @param hasReview      whether the user has reviewed the game
 * @param lastPlayRating the score from the most recent scored play log, or null
 */
public record GameInteractionStatusDto(
        boolean inWishlist,
        boolean inBacklog,
        boolean inLibrary,
        GameStatus libraryStatus,
        int playCount,
        Integer userRating,
        boolean hasReview,
        Integer lastPlayRating
) {
}
