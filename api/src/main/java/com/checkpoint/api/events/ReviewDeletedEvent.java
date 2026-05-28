package com.checkpoint.api.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published after a user deletes one of their own reviews.
 *
 * <p>{@code reviewCreatedAt} is captured before deletion so downstream consumers
 * (notably the {@code MISSION_FAILED} easter-egg badge) can compute how long the
 * review lived before being deleted.</p>
 */
public class ReviewDeletedEvent {

    private final UUID userId;
    private final UUID reviewId;
    private final LocalDateTime reviewCreatedAt;

    public ReviewDeletedEvent(UUID userId, UUID reviewId, LocalDateTime reviewCreatedAt) {
        this.userId = userId;
        this.reviewId = reviewId;
        this.reviewCreatedAt = reviewCreatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getReviewId() {
        return reviewId;
    }

    public LocalDateTime getReviewCreatedAt() {
        return reviewCreatedAt;
    }
}
