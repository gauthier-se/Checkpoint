package com.checkpoint.api.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.checkpoint.api.events.GameFinishedEvent;
import com.checkpoint.api.events.ReviewCreatedEvent;
import com.checkpoint.api.services.GamificationService;

/**
 * Listens for gamification-related events and delegates XP awards
 * to the {@link GamificationService}.
 *
 * <p>All handlers are asynchronous to avoid blocking the publishing thread.</p>
 */
@Component
public class GamificationListener {

    private static final Logger log = LoggerFactory.getLogger(GamificationListener.class);

    private static final int REVIEW_XP = 50;
    private static final int GAME_FINISHED_XP = 100;

    private final GamificationService gamificationService;

    /**
     * Constructs a new GamificationListener.
     *
     * @param gamificationService the gamification service
     */
    public GamificationListener(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    /**
     * Handles a {@link ReviewCreatedEvent} by awarding XP to the user.
     *
     * @param event the review created event
     */
    @Async
    @EventListener
    public void onReviewCreated(ReviewCreatedEvent event) {
        log.info("Handling ReviewCreatedEvent for user {}", event.getUserId());
        gamificationService.addXp(event.getUserId(), REVIEW_XP);
    }

    /**
     * Handles a {@link GameFinishedEvent} by awarding XP to the user.
     *
     * @param event the game finished event
     */
    @Async
    @EventListener
    public void onGameFinished(GameFinishedEvent event) {
        log.info("Handling GameFinishedEvent for user {}", event.getUserId());
        gamificationService.addXp(event.getUserId(), GAME_FINISHED_XP);
    }
}
