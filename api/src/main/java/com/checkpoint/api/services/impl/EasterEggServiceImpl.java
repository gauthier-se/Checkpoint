package com.checkpoint.api.services.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.ReviewView;
import com.checkpoint.api.enums.BadgeCode;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.ReviewViewRepository;
import com.checkpoint.api.services.BadgeAwardingService;
import com.checkpoint.api.services.EasterEggService;

/**
 * Implementation of {@link EasterEggService}.
 *
 * <p>The bell-click counter is intentionally in-memory and per-process: the
 * HEY_LISTEN badge is meant as "click furiously in a single session", not a
 * lifetime counter. Resetting on restart is acceptable for a fun badge.</p>
 */
@Service
public class EasterEggServiceImpl implements EasterEggService {

    private static final Logger log = LoggerFactory.getLogger(EasterEggServiceImpl.class);

    private static final int HEY_LISTEN_THRESHOLD = 50;

    private final BadgeAwardingService badgeAwardingService;
    private final ReviewRepository reviewRepository;
    private final ReviewViewRepository reviewViewRepository;

    private final ConcurrentMap<UUID, AtomicInteger> bellClicksByUser = new ConcurrentHashMap<>();

    public EasterEggServiceImpl(BadgeAwardingService badgeAwardingService,
                                ReviewRepository reviewRepository,
                                ReviewViewRepository reviewViewRepository) {
        this.badgeAwardingService = badgeAwardingService;
        this.reviewRepository = reviewRepository;
        this.reviewViewRepository = reviewViewRepository;
    }

    @Override
    public void recordKonami(UUID userId) {
        badgeAwardingService.awardIfEligible(userId, BadgeCode.KONAMI);
    }

    @Override
    public void recordBarrelRoll(UUID userId) {
        badgeAwardingService.awardIfEligible(userId, BadgeCode.BARREL_ROLL);
    }

    @Override
    public void recordRickroll(UUID userId) {
        badgeAwardingService.awardIfEligible(userId, BadgeCode.RICKROLL);
    }

    @Override
    public void recordBellClick(UUID userId) {
        int clicks = bellClicksByUser
                .computeIfAbsent(userId, k -> new AtomicInteger(0))
                .incrementAndGet();
        if (clicks == HEY_LISTEN_THRESHOLD) {
            log.info("HEY_LISTEN threshold reached for user {} ({} clicks this session)", userId, clicks);
            badgeAwardingService.awardIfEligible(userId, BadgeCode.HEY_LISTEN);
        }
    }

    @Override
    @Transactional
    public void recordReviewView(UUID viewerId, UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));
        if (review.getUser() == null || viewerId.equals(review.getUser().getId())) {
            // Only views of other users' reviews count toward the badge.
            return;
        }
        if (reviewViewRepository.existsByUserIdAndReviewId(viewerId, reviewId)) {
            return;
        }
        reviewViewRepository.save(new ReviewView(viewerId, reviewId));
        badgeAwardingService.checkReaderBadges(viewerId);
    }
}
