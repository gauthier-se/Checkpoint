package com.checkpoint.api.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository for querying the activity feed across multiple tables.
 */
public interface FeedRepository {

    /**
     * Finds recent activity items from the given followed users.
     * Aggregates play sessions, ratings, reviews, and list creations
     * using a UNION ALL native query.
     *
     * @param followingIds the IDs of users being followed
     * @param since        the start of the time window
     * @param pageable     pagination parameters
     * @return a page of raw result rows (id, type, created_at, user_id, video_game_id, extra1, extra2)
     */
    Page<Object[]> findFeedItems(List<UUID> followingIds, LocalDateTime since, Pageable pageable);
}
