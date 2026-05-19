package com.checkpoint.api.listeners;

import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.events.GameFinishedEvent;
import com.checkpoint.api.events.PlayLogCreatedEvent;
import com.checkpoint.api.events.ReviewCreatedEvent;
import com.checkpoint.api.events.ReviewLikedEvent;
import com.checkpoint.api.events.UserActivityEvent;
import com.checkpoint.api.events.UserFollowedEvent;
import com.checkpoint.api.events.UserGainedFollowerEvent;
import com.checkpoint.api.events.UserLeveledUpEvent;
import com.checkpoint.api.services.BadgeAwardingService;

/**
 * Unit tests for {@link BadgeListener}.
 */
@ExtendWith(MockitoExtension.class)
class BadgeListenerTest {

    @Mock
    private BadgeAwardingService badgeAwardingService;

    private BadgeListener badgeListener;

    @BeforeEach
    void setUp() {
        badgeListener = new BadgeListener(badgeAwardingService);
    }

    @Nested
    @DisplayName("onReviewCreated()")
    class OnReviewCreated {

        @Test
        @DisplayName("Should evaluate review-count and review-quality badges for the user")
        void shouldDelegateToCheckReviewBadgesAndQuality() {
            // Given
            UUID userId = UUID.randomUUID();
            ReviewCreatedEvent event = new ReviewCreatedEvent(userId);

            // When
            badgeListener.onReviewCreated(event);

            // Then
            verify(badgeAwardingService).checkReviewBadges(userId);
            verify(badgeAwardingService).checkReviewQualityBadges(userId);
        }
    }

    @Nested
    @DisplayName("onGameFinished()")
    class OnGameFinished {

        @Test
        @DisplayName("Should evaluate game-finished and genre badges for the user")
        void shouldDelegateToCheckGameFinishedAndGenreBadges() {
            // Given
            UUID userId = UUID.randomUUID();
            GameFinishedEvent event = new GameFinishedEvent(userId);

            // When
            badgeListener.onGameFinished(event);

            // Then
            verify(badgeAwardingService).checkGameFinishedBadges(userId);
            verify(badgeAwardingService).checkGenreBadges(userId);
        }
    }

    @Nested
    @DisplayName("onUserLeveledUp()")
    class OnUserLeveledUp {

        @Test
        @DisplayName("Should evaluate level badges with the new level")
        void shouldDelegateToCheckLevelBadges() {
            // Given
            UUID userId = UUID.randomUUID();
            int newLevel = 5;
            UserLeveledUpEvent event = new UserLeveledUpEvent(userId, newLevel);

            // When
            badgeListener.onUserLeveledUp(event);

            // Then
            verify(badgeAwardingService).checkLevelBadges(userId, newLevel);
        }
    }

    @Nested
    @DisplayName("onPlayLogCreated()")
    class OnPlayLogCreated {

        @Test
        @DisplayName("Should evaluate play-log and library-size badges for the user")
        void shouldDelegateToPlayLogAndLibraryChecks() {
            // Given
            UUID userId = UUID.randomUUID();
            PlayLogCreatedEvent event = new PlayLogCreatedEvent(userId);

            // When
            badgeListener.onPlayLogCreated(event);

            // Then
            verify(badgeAwardingService).checkPlayLogBadges(userId);
            verify(badgeAwardingService).checkLibrarySizeBadges(userId);
        }
    }

    @Nested
    @DisplayName("onReviewLiked()")
    class OnReviewLiked {

        @Test
        @DisplayName("Should evaluate social badges for both liker and review author")
        void shouldDelegateToSocialChecksForBothParties() {
            // Given
            UUID likerId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();
            ReviewLikedEvent event = new ReviewLikedEvent(
                    likerId, authorId, UUID.randomUUID(), UUID.randomUUID());

            // When
            badgeListener.onReviewLiked(event);

            // Then
            verify(badgeAwardingService).checkSocialBadges(likerId);
            verify(badgeAwardingService).checkSocialBadges(authorId);
        }
    }

    @Nested
    @DisplayName("onUserFollowed()")
    class OnUserFollowed {

        @Test
        @DisplayName("Should evaluate social badges for the follower")
        void shouldDelegateToSocialBadgesForFollower() {
            // Given
            UUID followerId = UUID.randomUUID();
            UUID followedId = UUID.randomUUID();
            UserFollowedEvent event = new UserFollowedEvent(followerId, followedId);

            // When
            badgeListener.onUserFollowed(event);

            // Then
            verify(badgeAwardingService).checkSocialBadges(followerId);
        }
    }

    @Nested
    @DisplayName("onUserGainedFollower()")
    class OnUserGainedFollower {

        @Test
        @DisplayName("Should evaluate social badges for the followed user")
        void shouldDelegateToSocialBadgesForFollowedUser() {
            // Given
            UUID followedId = UUID.randomUUID();
            UUID followerId = UUID.randomUUID();
            UserGainedFollowerEvent event = new UserGainedFollowerEvent(followedId, followerId);

            // When
            badgeListener.onUserGainedFollower(event);

            // Then
            verify(badgeAwardingService).checkSocialBadges(followedId);
        }
    }

    @Nested
    @DisplayName("onUserActivity()")
    class OnUserActivity {

        @Test
        @DisplayName("Should evaluate longevity badges for the user")
        void shouldDelegateToLongevityBadges() {
            // Given
            UUID userId = UUID.randomUUID();
            UserActivityEvent event = new UserActivityEvent(userId);

            // When
            badgeListener.onUserActivity(event);

            // Then
            verify(badgeAwardingService).checkLongevityBadges(userId);
        }
    }
}
