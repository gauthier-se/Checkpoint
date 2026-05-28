package com.checkpoint.api.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Records that a user has opened a specific review at least once. Used by the
 * STAY_AWHILE_REVIEWS easter-egg badge to count distinct reviews read. A
 * composite primary key on {@code (user_id, review_id)} keeps repeat views
 * from inflating the count.
 */
@Entity
@Table(name = "review_views")
@IdClass(ReviewView.PK.class)
public class ReviewView {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "review_id")
    private UUID reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review review;

    public ReviewView() {}

    public ReviewView(UUID userId, UUID reviewId) {
        this.userId = userId;
        this.reviewId = reviewId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getReviewId() {
        return reviewId;
    }

    public void setReviewId(UUID reviewId) {
        this.reviewId = reviewId;
    }

    public static class PK implements Serializable {
        private UUID userId;
        private UUID reviewId;

        public PK() {}

        public PK(UUID userId, UUID reviewId) {
            this.userId = userId;
            this.reviewId = reviewId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK other)) return false;
            return Objects.equals(userId, other.userId) && Objects.equals(reviewId, other.reviewId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, reviewId);
        }
    }
}
