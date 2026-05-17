package com.checkpoint.api.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.checkpoint.api.events.GameFinishedEvent;
import com.checkpoint.api.events.ReviewCreatedEvent;
import com.checkpoint.api.events.UserLeveledUpEvent;
import com.checkpoint.api.services.BadgeAwardingService;

/**
 * Listens for domain events and delegates badge evaluation to {@link BadgeAwardingService}.
 *
 * <p>Uses {@link TransactionalEventListener} with {@link TransactionPhase#AFTER_COMMIT}
 * so that listeners only fire once the publishing transaction has been committed.
 * This guarantees the count queries in the service (e.g. {@code countByUserId})
 * include the freshly persisted row that triggered the event.</p>
 *
 * <p>Listeners are also {@link Async} to avoid blocking the publishing thread.</p>
 */
@Component
public class BadgeListener {

    private static final Logger log = LoggerFactory.getLogger(BadgeListener.class);

    private final BadgeAwardingService badgeAwardingService;

    public BadgeListener(BadgeAwardingService badgeAwardingService) {
        this.badgeAwardingService = badgeAwardingService;
    }

    /**
     * Handles a {@link ReviewCreatedEvent} by evaluating review-count badges.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewCreated(ReviewCreatedEvent event) {
        log.info("Handling ReviewCreatedEvent for badge evaluation, user {}", event.getUserId());
        badgeAwardingService.checkReviewBadges(event.getUserId());
    }

    /**
     * Handles a {@link GameFinishedEvent} by evaluating completion-count badges.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameFinished(GameFinishedEvent event) {
        log.info("Handling GameFinishedEvent for badge evaluation, user {}", event.getUserId());
        badgeAwardingService.checkGameFinishedBadges(event.getUserId());
    }

    /**
     * Handles a {@link UserLeveledUpEvent} by evaluating level-based badges.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserLeveledUp(UserLeveledUpEvent event) {
        log.info("Handling UserLeveledUpEvent for badge evaluation, user {} now at level {}",
                event.getUserId(), event.getNewLevel());
        badgeAwardingService.checkLevelBadges(event.getUserId(), event.getNewLevel());
    }
}
