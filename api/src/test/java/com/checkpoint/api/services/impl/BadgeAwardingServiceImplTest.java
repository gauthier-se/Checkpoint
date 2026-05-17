package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.checkpoint.api.entities.Badge;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.enums.BadgeCode;
import com.checkpoint.api.enums.GameStatus;
import com.checkpoint.api.events.BadgeUnlockedEvent;
import com.checkpoint.api.repositories.BadgeRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserGameRepository;
import com.checkpoint.api.repositories.UserRepository;

/**
 * Unit tests for {@link BadgeAwardingServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class BadgeAwardingServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private BadgeRepository badgeRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private UserGameRepository userGameRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private BadgeAwardingServiceImpl service;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        service = new BadgeAwardingServiceImpl(
                userRepository, badgeRepository, reviewRepository,
                userGameRepository, eventPublisher);

        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
    }

    private Badge badge(BadgeCode code) {
        Badge b = new Badge(code.name(), code.getDefaultName(),
                code.getDefaultDescription(), null);
        b.setId(UUID.randomUUID());
        return b;
    }

    @Nested
    @DisplayName("awardIfEligible()")
    class AwardIfEligible {

        @Test
        @DisplayName("Should add the badge and publish the event when the user does not own it")
        void shouldAwardWhenEligible() {
            // Given
            Badge badge = badge(BadgeCode.FIRST_REVIEW);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.FIRST_REVIEW.name()))
                    .thenReturn(Optional.of(badge));

            // When
            service.awardIfEligible(userId, BadgeCode.FIRST_REVIEW);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getBadges()).contains(badge);

            ArgumentCaptor<BadgeUnlockedEvent> eventCaptor =
                    ArgumentCaptor.forClass(BadgeUnlockedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getUserId()).isEqualTo(userId);
            assertThat(eventCaptor.getValue().getCode()).isEqualTo(BadgeCode.FIRST_REVIEW);
        }

        @Test
        @DisplayName("Should be a no-op when the user already owns the badge")
        void shouldBeIdempotent() {
            // Given
            Badge badge = badge(BadgeCode.FIRST_REVIEW);
            user.getBadges().add(badge);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.FIRST_REVIEW.name()))
                    .thenReturn(Optional.of(badge));

            // When
            service.awardIfEligible(userId, BadgeCode.FIRST_REVIEW);

            // Then
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw when the user does not exist")
        void shouldThrowWhenUserMissing() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.awardIfEligible(userId, BadgeCode.FIRST_REVIEW))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("Should be a no-op when the badge code is missing from the catalog")
        void shouldNoOpWhenBadgeMissing() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.FIRST_REVIEW.name()))
                    .thenReturn(Optional.empty());

            // When
            service.awardIfEligible(userId, BadgeCode.FIRST_REVIEW);

            // Then
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("checkReviewBadges()")
    class CheckReviewBadges {

        @Test
        @DisplayName("Should award only FIRST_REVIEW when review count is 1")
        void shouldAwardFirstReviewOnly() {
            // Given
            when(reviewRepository.countByUserId(userId)).thenReturn(1L);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.FIRST_REVIEW.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.FIRST_REVIEW)));

            // When
            service.checkReviewBadges(userId);

            // Then
            verify(badgeRepository).findByCode(BadgeCode.FIRST_REVIEW.name());
            verify(badgeRepository, never()).findByCode(BadgeCode.REVIEW_10.name());
            verify(badgeRepository, never()).findByCode(BadgeCode.REVIEW_50.name());
        }

        @Test
        @DisplayName("Should evaluate FIRST_REVIEW and REVIEW_10 when count is 10")
        void shouldEvaluateFirstReviewAndReview10AtTen() {
            // Given
            when(reviewRepository.countByUserId(userId)).thenReturn(10L);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.FIRST_REVIEW.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.FIRST_REVIEW)));
            when(badgeRepository.findByCode(BadgeCode.REVIEW_10.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.REVIEW_10)));

            // When
            service.checkReviewBadges(userId);

            // Then
            verify(badgeRepository).findByCode(BadgeCode.FIRST_REVIEW.name());
            verify(badgeRepository).findByCode(BadgeCode.REVIEW_10.name());
            verify(badgeRepository, never()).findByCode(BadgeCode.REVIEW_50.name());
        }
    }

    @Nested
    @DisplayName("checkGameFinishedBadges()")
    class CheckGameFinishedBadges {

        @Test
        @DisplayName("Should award only FIRST_GAME_FINISHED when completed count is 1")
        void shouldAwardFirstGameFinishedOnly() {
            // Given
            when(userGameRepository.countByUserIdAndStatus(userId, GameStatus.COMPLETED))
                    .thenReturn(1L);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.FIRST_GAME_FINISHED.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.FIRST_GAME_FINISHED)));

            // When
            service.checkGameFinishedBadges(userId);

            // Then
            verify(badgeRepository).findByCode(BadgeCode.FIRST_GAME_FINISHED.name());
            verify(badgeRepository, never()).findByCode(BadgeCode.GAME_FINISHED_10.name());
            verify(badgeRepository, never()).findByCode(BadgeCode.GAME_FINISHED_50.name());
        }

        @Test
        @DisplayName("Should evaluate all three tiers when count is 50")
        void shouldEvaluateAllTiersAtFifty() {
            // Given
            when(userGameRepository.countByUserIdAndStatus(userId, GameStatus.COMPLETED))
                    .thenReturn(50L);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.FIRST_GAME_FINISHED.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.FIRST_GAME_FINISHED)));
            when(badgeRepository.findByCode(BadgeCode.GAME_FINISHED_10.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.GAME_FINISHED_10)));
            when(badgeRepository.findByCode(BadgeCode.GAME_FINISHED_50.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.GAME_FINISHED_50)));

            // When
            service.checkGameFinishedBadges(userId);

            // Then
            verify(badgeRepository).findByCode(BadgeCode.FIRST_GAME_FINISHED.name());
            verify(badgeRepository).findByCode(BadgeCode.GAME_FINISHED_10.name());
            verify(badgeRepository).findByCode(BadgeCode.GAME_FINISHED_50.name());
        }
    }

    @Nested
    @DisplayName("checkLevelBadges()")
    class CheckLevelBadges {

        @Test
        @DisplayName("Should award LEVEL_5 only at level 5")
        void shouldAwardLevel5Only() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.LEVEL_5.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.LEVEL_5)));

            // When
            service.checkLevelBadges(userId, 5);

            // Then
            verify(badgeRepository).findByCode(BadgeCode.LEVEL_5.name());
            verify(badgeRepository, never()).findByCode(BadgeCode.LEVEL_10.name());
            verify(badgeRepository, never()).findByCode(BadgeCode.LEVEL_25.name());
        }

        @Test
        @DisplayName("Should evaluate all three level badges at level 25")
        void shouldEvaluateAllLevelBadgesAt25() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(badgeRepository.findByCode(BadgeCode.LEVEL_5.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.LEVEL_5)));
            when(badgeRepository.findByCode(BadgeCode.LEVEL_10.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.LEVEL_10)));
            when(badgeRepository.findByCode(BadgeCode.LEVEL_25.name()))
                    .thenReturn(Optional.of(badge(BadgeCode.LEVEL_25)));

            // When
            service.checkLevelBadges(userId, 25);

            // Then
            verify(badgeRepository).findByCode(BadgeCode.LEVEL_5.name());
            verify(badgeRepository).findByCode(BadgeCode.LEVEL_10.name());
            verify(badgeRepository).findByCode(BadgeCode.LEVEL_25.name());
        }

        @Test
        @DisplayName("Should award no badges below level 5")
        void shouldAwardNothingBelow5() {
            // When
            service.checkLevelBadges(userId, 4);

            // Then
            verify(badgeRepository, never()).findByCode(any());
            verify(userRepository, never()).findById(any());
        }
    }
}
