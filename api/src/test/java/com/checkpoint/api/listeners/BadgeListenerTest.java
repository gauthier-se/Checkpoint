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
import com.checkpoint.api.events.ReviewCreatedEvent;
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
        @DisplayName("Should evaluate review badges for the user")
        void shouldDelegateToCheckReviewBadges() {
            // Given
            UUID userId = UUID.randomUUID();
            ReviewCreatedEvent event = new ReviewCreatedEvent(userId);

            // When
            badgeListener.onReviewCreated(event);

            // Then
            verify(badgeAwardingService).checkReviewBadges(userId);
        }
    }

    @Nested
    @DisplayName("onGameFinished()")
    class OnGameFinished {

        @Test
        @DisplayName("Should evaluate game-finished badges for the user")
        void shouldDelegateToCheckGameFinishedBadges() {
            // Given
            UUID userId = UUID.randomUUID();
            GameFinishedEvent event = new GameFinishedEvent(userId);

            // When
            badgeListener.onGameFinished(event);

            // Then
            verify(badgeAwardingService).checkGameFinishedBadges(userId);
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
}
