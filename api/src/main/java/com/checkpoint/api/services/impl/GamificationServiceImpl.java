package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.entities.User;
import com.checkpoint.api.events.UserLeveledUpEvent;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.GamificationService;

/**
 * Implementation of {@link GamificationService}.
 * Manages XP awards and automatic level progression.
 */
@Service
@Transactional
public class GamificationServiceImpl implements GamificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationServiceImpl.class);

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructs a new GamificationServiceImpl.
     *
     * @param userRepository the user repository
     * @param eventPublisher Spring's application event publisher, used to broadcast level-ups
     */
    public GamificationServiceImpl(UserRepository userRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addXp(UUID userId, int xpAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        Integer currentXp = user.getXpPoint();
        Integer newXp = currentXp + xpAmount;
        user.setXpPoint(newXp);

        Integer currentLevel = user.getLevel();
        Integer newLevel = currentLevel;

        while (newXp >= newLevel * 1000) {
            newLevel++;
        }

        boolean leveledUp = newLevel > currentLevel;
        if (leveledUp) {
            user.setLevel(newLevel);
            log.info("User {} leveled up from {} to {} (XP: {})", userId, currentLevel, newLevel, newXp);
        }

        userRepository.save(user);
        log.info("Awarded {} XP to user {} (total: {})", xpAmount, userId, newXp);

        if (leveledUp) {
            eventPublisher.publishEvent(new UserLeveledUpEvent(userId, newLevel));
        }
    }
}
