package com.checkpoint.api.mapper;

import java.util.Map;
import java.util.UUID;

import com.checkpoint.api.dto.social.FeedItemDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;

/**
 * Mapper for converting raw feed query results into {@link FeedItemDto} records.
 */
public interface FeedMapper {

    /**
     * Maps a native SQL result row to a FeedItemDto.
     * Resolves user and game information from pre-fetched caches.
     *
     * @param row       the result row (id, type, created_at, user_id, video_game_id, extra1, extra2)
     * @param userCache pre-fetched users keyed by ID
     * @param gameCache pre-fetched video games keyed by ID
     * @return the mapped FeedItemDto, or null if user is not found in cache
     */
    FeedItemDto toFeedItemDto(Object[] row, Map<UUID, User> userCache, Map<UUID, VideoGame> gameCache);
}
