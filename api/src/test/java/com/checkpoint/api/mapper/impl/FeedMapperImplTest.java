package com.checkpoint.api.mapper.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.checkpoint.api.dto.social.FeedItemDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.enums.FeedItemType;

/**
 * Unit tests for {@link FeedMapperImpl}.
 */
class FeedMapperImplTest {

    private FeedMapperImpl feedMapper;

    private User user;
    private VideoGame game;

    @BeforeEach
    void setUp() {
        feedMapper = new FeedMapperImpl();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setPseudo("frienduser");
        user.setPicture("avatar.jpg");

        game = new VideoGame();
        game.setId(UUID.randomUUID());
        game.setTitle("Hollow Knight");
        game.setCoverUrl("cover.jpg");
    }

    @Test
    @DisplayName("should map a LIKE_GAME row to a feed item carrying the liked game")
    void shouldMapLikeGameRow() {
        UUID likeId = UUID.randomUUID();
        Object[] row = new Object[]{
                likeId, "LIKE_GAME", Timestamp.valueOf(LocalDateTime.now()),
                user.getId(), game.getId(), null, null, null
        };

        FeedItemDto result = feedMapper.toFeedItemDto(
                row,
                Map.of(user.getId(), user),
                Map.of(game.getId(), game));

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(FeedItemType.LIKE_GAME);
        assertThat(result.user().id()).isEqualTo(user.getId());
        assertThat(result.game()).isNotNull();
        assertThat(result.game().id()).isEqualTo(game.getId());
        assertThat(result.game().title()).isEqualTo("Hollow Knight");
        // No type-specific fields for a game like.
        assertThat(result.playStatus()).isNull();
        assertThat(result.score()).isNull();
        assertThat(result.reviewContent()).isNull();
        assertThat(result.listTitle()).isNull();
    }

    @Test
    @DisplayName("should return null when the acting user is missing from the cache")
    void shouldReturnNullWhenUserMissing() {
        Object[] row = new Object[]{
                UUID.randomUUID(), "LIKE_GAME", Timestamp.valueOf(LocalDateTime.now()),
                user.getId(), game.getId(), null, null, null
        };

        FeedItemDto result = feedMapper.toFeedItemDto(row, Map.of(), Map.of(game.getId(), game));

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should map a REVIEW row carrying the associated play-log id")
    void shouldMapReviewRowWithLogId() {
        UUID reviewId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        Object[] row = new Object[]{
                reviewId, "REVIEW", Timestamp.valueOf(LocalDateTime.now()),
                user.getId(), game.getId(), "Great game!", "false", logId.toString()
        };

        FeedItemDto result = feedMapper.toFeedItemDto(
                row,
                Map.of(user.getId(), user),
                Map.of(game.getId(), game));

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(FeedItemType.REVIEW);
        assertThat(result.reviewContent()).isEqualTo("Great game!");
        assertThat(result.haveSpoilers()).isFalse();
        assertThat(result.logId()).isEqualTo(logId);
    }
}
