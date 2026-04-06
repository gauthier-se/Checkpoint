package com.checkpoint.api.services;

import java.util.List;

import com.checkpoint.api.dto.catalog.GameCardDto;

/**
 * Service interface for trending games operations.
 * Provides methods for retrieving games ranked by recent user activity.
 */
public interface GameTrendingService {

    /**
     * Returns the trending games based on recent user activity.
     * Scores games by weighted recent library additions, play sessions,
     * ratings, reviews, likes, and wishlist additions from the last 7 days.
     * Falls back to all-time popularity when recent activity is insufficient.
     *
     * @param size the maximum number of games to return
     * @return a list of trending game cards
     */
    List<GameCardDto> getTrendingGames(int size);
}
