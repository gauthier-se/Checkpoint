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
import com.checkpoint.api.services.GamificationService;

/**
 * Unit tests for {@link GamificationListener}.
 */
@ExtendWith(MockitoExtension.class)
class GamificationListenerTest {

    @Mock
    private GamificationService gamificationService;

    private GamificationListener gamificationListener;

    @BeforeEach
    void setUp() {
        gamificationListener = new GamificationListener(gamificationService);
    }

    @Nested
    @DisplayName("onReviewCreated()")
    class OnReviewCreated {

        @Test
        @DisplayName("Should award 50 XP when a review is created")
        void onReviewCreated_shouldAward50Xp() {
            // Given
            UUID userId = UUID.randomUUID();
            ReviewCreatedEvent event = new ReviewCreatedEvent(userId);

            // When
            gamificationListener.onReviewCreated(event);

            // Then
            verify(gamificationService).addXp(userId, 50);
        }
    }

    @Nested
    @DisplayName("onGameFinished()")
    class OnGameFinished {

        @Test
        @DisplayName("Should award 100 XP when a game is finished")
        void onGameFinished_shouldAward100Xp() {
            // Given
            UUID userId = UUID.randomUUID();
            GameFinishedEvent event = new GameFinishedEvent(userId);

            // When
            gamificationListener.onGameFinished(event);

            // Then
            verify(gamificationService).addXp(userId, 100);
        }
    }
}
