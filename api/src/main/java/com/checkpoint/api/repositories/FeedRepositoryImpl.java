package com.checkpoint.api.repositories;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.enums.FeedItemType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * Implementation of {@link FeedRepository}.
 * Uses native SQL UNION ALL queries across multiple activity tables.
 */
@Repository
public class FeedRepositoryImpl implements FeedRepository {

    private static final String FEED_QUERY = """
            SELECT id, type, created_at, user_id, video_game_id, extra1, extra2, extra3
            FROM (
                SELECT ugp.id, 'PLAY' AS type, ugp.created_at, ugp.user_id, ugp.video_game_id,
                       ugp.status AS extra1, NULL AS extra2, NULL AS extra3
                FROM user_game_plays ugp
                WHERE ugp.user_id IN (:followingIds) AND ugp.created_at >= :since
                UNION ALL
                SELECT r.id, 'RATING', r.created_at, r.user_id, r.video_game_id,
                       CAST(r.score AS VARCHAR), NULL, NULL
                FROM rates r
                WHERE r.user_id IN (:followingIds) AND r.created_at >= :since
                UNION ALL
                SELECT rv.id, 'REVIEW', rv.created_at, rv.user_id, rv.video_game_id,
                       LEFT(rv.content, 200), CAST(rv.have_spoilers AS VARCHAR),
                       CAST(rv.user_game_play_id AS VARCHAR)
                FROM reviews rv
                WHERE rv.user_id IN (:followingIds) AND rv.created_at >= :since
                UNION ALL
                SELECT l.id, 'LIST', l.created_at, l.user_id, NULL,
                       l.title, CAST((SELECT COUNT(*) FROM game_list_entries gle WHERE gle.list_id = l.id) AS VARCHAR), NULL
                FROM lists l
                WHERE l.user_id IN (:followingIds) AND l.created_at >= :since AND l.is_private = false
                UNION ALL
                SELECT lk.id, 'LIKE_GAME', lk.created_at, lk.user_id, lk.video_game_id,
                       NULL, NULL, NULL
                FROM likes lk
                WHERE lk.user_id IN (:followingIds) AND lk.created_at >= :since
                  AND lk.video_game_id IS NOT NULL
            ) AS feed
            WHERE (CAST(:type AS VARCHAR) IS NULL OR feed.type = CAST(:type AS VARCHAR))
            ORDER BY created_at DESC
            """;

    private static final String COUNT_QUERY = """
            SELECT COUNT(*) FROM (
                SELECT ugp.id, 'PLAY' AS type FROM user_game_plays ugp
                WHERE ugp.user_id IN (:followingIds) AND ugp.created_at >= :since
                UNION ALL
                SELECT r.id, 'RATING' FROM rates r
                WHERE r.user_id IN (:followingIds) AND r.created_at >= :since
                UNION ALL
                SELECT rv.id, 'REVIEW' FROM reviews rv
                WHERE rv.user_id IN (:followingIds) AND rv.created_at >= :since
                UNION ALL
                SELECT l.id, 'LIST' FROM lists l
                WHERE l.user_id IN (:followingIds) AND l.created_at >= :since AND l.is_private = false
                UNION ALL
                SELECT lk.id, 'LIKE_GAME' FROM likes lk
                WHERE lk.user_id IN (:followingIds) AND lk.created_at >= :since
                  AND lk.video_game_id IS NOT NULL
            ) AS feed_count
            WHERE (CAST(:type AS VARCHAR) IS NULL OR feed_count.type = CAST(:type AS VARCHAR))
            """;

    private final EntityManager entityManager;

    public FeedRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<Object[]> findFeedItems(List<UUID> followingIds, LocalDateTime since, FeedItemType type, Pageable pageable) {
        if (followingIds == null || followingIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // Bind the enum as its name (String) so the nullable native parameter resolves cleanly.
        String typeParam = (type != null) ? type.name() : null;

        Query countNativeQuery = entityManager.createNativeQuery(COUNT_QUERY);
        countNativeQuery.setParameter("followingIds", followingIds);
        countNativeQuery.setParameter("since", since);
        countNativeQuery.setParameter("type", typeParam);
        long total = ((Number) countNativeQuery.getSingleResult()).longValue();

        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Query feedNativeQuery = entityManager.createNativeQuery(FEED_QUERY);
        feedNativeQuery.setParameter("followingIds", followingIds);
        feedNativeQuery.setParameter("since", since);
        feedNativeQuery.setParameter("type", typeParam);
        feedNativeQuery.setFirstResult((int) pageable.getOffset());
        feedNativeQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = feedNativeQuery.getResultList();

        return new PageImpl<>(results, pageable, total);
    }
}
