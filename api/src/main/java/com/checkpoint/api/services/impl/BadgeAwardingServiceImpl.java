package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.entities.Badge;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.enums.BadgeCode;
import com.checkpoint.api.enums.GameStatus;
import com.checkpoint.api.events.BadgeUnlockedEvent;
import com.checkpoint.api.repositories.BadgeRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserGameRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.BadgeAwardingService;

/**
 * Implementation of {@link BadgeAwardingService}.
 */
@Service
@Transactional
public class BadgeAwardingServiceImpl implements BadgeAwardingService {

    private static final Logger log = LoggerFactory.getLogger(BadgeAwardingServiceImpl.class);

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final ReviewRepository reviewRepository;
    private final UserGameRepository userGameRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BadgeAwardingServiceImpl(UserRepository userRepository,
                                    BadgeRepository badgeRepository,
                                    ReviewRepository reviewRepository,
                                    UserGameRepository userGameRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.reviewRepository = reviewRepository;
        this.userGameRepository = userGameRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void awardIfEligible(UUID userId, BadgeCode code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        Badge badge = badgeRepository.findByCode(code.name()).orElse(null);
        if (badge == null) {
            log.warn("Badge code {} not found in DB — skipping award for user {}. Seed the badge catalog.",
                    code, userId);
            return;
        }

        if (user.getBadges().contains(badge)) {
            return;
        }

        user.addBadge(badge);
        userRepository.save(user);
        log.info("Awarded badge {} to user {}", code, userId);
        eventPublisher.publishEvent(new BadgeUnlockedEvent(userId, code));
    }

    @Override
    public void checkReviewBadges(UUID userId) {
        long count = reviewRepository.countByUserId(userId);
        if (count >= 1) {
            awardIfEligible(userId, BadgeCode.FIRST_REVIEW);
        }
        if (count >= 10) {
            awardIfEligible(userId, BadgeCode.REVIEW_10);
        }
        if (count >= 50) {
            awardIfEligible(userId, BadgeCode.REVIEW_50);
        }
    }

    @Override
    public void checkGameFinishedBadges(UUID userId) {
        long count = userGameRepository.countByUserIdAndStatus(userId, GameStatus.COMPLETED);
        if (count >= 1) {
            awardIfEligible(userId, BadgeCode.FIRST_GAME_FINISHED);
        }
        if (count >= 10) {
            awardIfEligible(userId, BadgeCode.GAME_FINISHED_10);
        }
        if (count >= 50) {
            awardIfEligible(userId, BadgeCode.GAME_FINISHED_50);
        }
    }

    @Override
    public void checkLevelBadges(UUID userId, int newLevel) {
        if (newLevel >= 5) {
            awardIfEligible(userId, BadgeCode.LEVEL_5);
        }
        if (newLevel >= 10) {
            awardIfEligible(userId, BadgeCode.LEVEL_10);
        }
        if (newLevel >= 25) {
            awardIfEligible(userId, BadgeCode.LEVEL_25);
        }
    }
}
