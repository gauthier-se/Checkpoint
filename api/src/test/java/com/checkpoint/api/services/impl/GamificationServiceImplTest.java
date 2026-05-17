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

import com.checkpoint.api.entities.User;
import com.checkpoint.api.events.UserLeveledUpEvent;
import com.checkpoint.api.repositories.UserRepository;

/**
 * Unit tests for {@link GamificationServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class GamificationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private GamificationServiceImpl gamificationService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        gamificationService = new GamificationServiceImpl(userRepository, eventPublisher);

        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setXpPoint(0);
        testUser.setLevel(1);
    }

    @Nested
    @DisplayName("addXp()")
    class AddXp {

        @Test
        @DisplayName("Should add XP to user without leveling up")
        void addXp_shouldAddXpWithoutLevelUp() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            gamificationService.addXp(userId, 50);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getXpPoint()).isEqualTo(50);
            assertThat(savedUser.getLevel()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should level up when XP reaches threshold")
        void addXp_shouldLevelUpWhenThresholdReached() {
            // Given
            testUser.setXpPoint(950);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            gamificationService.addXp(userId, 50);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getXpPoint()).isEqualTo(1000);
            assertThat(savedUser.getLevel()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle multiple level ups at once")
        void addXp_shouldHandleMultipleLevelUps() {
            // Given
            testUser.setXpPoint(900);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            gamificationService.addXp(userId, 2100);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getXpPoint()).isEqualTo(3000);
            assertThat(savedUser.getLevel()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should not level up when XP is below threshold")
        void addXp_shouldNotLevelUpBelowThreshold() {
            // Given
            testUser.setXpPoint(800);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            gamificationService.addXp(userId, 100);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getXpPoint()).isEqualTo(900);
            assertThat(savedUser.getLevel()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should level up correctly at higher levels")
        void addXp_shouldLevelUpCorrectlyAtHigherLevels() {
            // Given
            testUser.setXpPoint(1900);
            testUser.setLevel(2);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            gamificationService.addXp(userId, 100);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getXpPoint()).isEqualTo(2000);
            assertThat(savedUser.getLevel()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void addXp_shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> gamificationService.addXp(userId, 50))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("Should publish UserLeveledUpEvent with the new level when leveling up")
        void addXp_shouldPublishLevelUpEvent() {
            // Given
            testUser.setXpPoint(950);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            gamificationService.addXp(userId, 50);

            // Then
            ArgumentCaptor<UserLeveledUpEvent> eventCaptor =
                    ArgumentCaptor.forClass(UserLeveledUpEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getUserId()).isEqualTo(userId);
            assertThat(eventCaptor.getValue().getNewLevel()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should not publish UserLeveledUpEvent when no level-up occurs")
        void addXp_shouldNotPublishWhenNoLevelUp() {
            // Given
            testUser.setXpPoint(100);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            gamificationService.addXp(userId, 50);

            // Then
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
