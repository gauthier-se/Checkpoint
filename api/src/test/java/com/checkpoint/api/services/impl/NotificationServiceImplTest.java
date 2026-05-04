package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.dto.notification.NotificationResponseDto;
import com.checkpoint.api.dto.notification.UnreadCountDto;
import com.checkpoint.api.entities.Notification;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.enums.NotificationType;
import com.checkpoint.api.exceptions.NotificationNotFoundException;
import com.checkpoint.api.exceptions.UnauthorizedNotificationAccessException;
import com.checkpoint.api.mapper.NotificationMapper;
import com.checkpoint.api.mapper.impl.NotificationMapperImpl;
import com.checkpoint.api.repositories.NotificationRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.NotificationPreferencesService;

/**
 * Unit tests for {@link NotificationServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationPreferencesService preferencesService;

    private NotificationMapper notificationMapper;
    private NotificationServiceImpl notificationService;

    private User recipient;
    private User sender;

    @BeforeEach
    void setUp() {
        notificationMapper = new NotificationMapperImpl();
        notificationService = new NotificationServiceImpl(
                notificationRepository, userRepository, notificationMapper, messagingTemplate, preferencesService);

        recipient = new User();
        recipient.setId(UUID.randomUUID());
        recipient.setEmail("recipient@example.com");
        recipient.setPseudo("recipientUser");

        sender = new User();
        sender.setId(UUID.randomUUID());
        sender.setEmail("sender@example.com");
        sender.setPseudo("senderUser");
        sender.setPicture("https://example.com/pic.jpg");
    }

    @Nested
    @DisplayName("createNotification")
    class CreateNotification {

        @Test
        @DisplayName("should persist notification and push via WebSocket")
        void createNotification_shouldPersistAndPushViaWebSocket() {
            // Given
            when(preferencesService.isEnabled(recipient.getId(), NotificationType.FOLLOW)).thenReturn(true);
            when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
            when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));

            Notification savedNotification = new Notification(
                    recipient, sender, NotificationType.FOLLOW, null, "senderUser started following you");
            savedNotification.setId(UUID.randomUUID());
            savedNotification.setCreatedAt(LocalDateTime.now());

            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

            // When
            NotificationResponseDto result = notificationService.createNotification(
                    recipient.getId(), sender.getId(), NotificationType.FOLLOW, null,
                    "senderUser started following you");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.senderPseudo()).isEqualTo("senderUser");
            assertThat(result.type()).isEqualTo(NotificationType.FOLLOW);
            assertThat(result.message()).isEqualTo("senderUser started following you");

            verify(notificationRepository).save(any(Notification.class));
            verify(messagingTemplate).convertAndSendToUser(
                    eq("recipient@example.com"),
                    eq("/queue/notifications"),
                    any(NotificationResponseDto.class)
            );
        }

        @Test
        @DisplayName("should return null when sender and recipient are the same user")
        void createNotification_shouldReturnNullForSelfNotification() {
            // Given
            UUID sameUserId = recipient.getId();

            // When
            NotificationResponseDto result = notificationService.createNotification(
                    sameUserId, sameUserId, NotificationType.FOLLOW, null, "Self follow");

            // Then
            assertThat(result).isNull();
            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("should return null when a duplicate notification already exists")
        void createNotification_shouldReturnNullForDuplicate() {
            // Given
            UUID referenceId = UUID.randomUUID();
            when(notificationRepository.existsBySenderIdAndRecipientIdAndTypeAndReferenceId(
                    sender.getId(), recipient.getId(), NotificationType.LIKE_REVIEW, referenceId))
                    .thenReturn(true);

            // When
            NotificationResponseDto result = notificationService.createNotification(
                    recipient.getId(), sender.getId(), NotificationType.LIKE_REVIEW, referenceId,
                    "senderUser liked your review");

            // Then
            assertThat(result).isNull();
            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("should skip persistence and WS push when type is disabled in preferences")
        void createNotification_shouldSkipWhenTypeDisabled() {
            // Given
            when(preferencesService.isEnabled(recipient.getId(), NotificationType.FOLLOW)).thenReturn(false);

            // When
            NotificationResponseDto result = notificationService.createNotification(
                    recipient.getId(), sender.getId(), NotificationType.FOLLOW, null,
                    "senderUser started following you");

            // Then
            assertThat(result).isNull();
            verify(notificationRepository, never()).save(any(Notification.class));
            verify(messagingTemplate, never()).convertAndSendToUser(
                    any(String.class), any(String.class), any(NotificationResponseDto.class));
        }

        @Test
        @DisplayName("should handle null sender for system notifications")
        void createNotification_shouldHandleNullSender() {
            // Given
            when(preferencesService.isEnabled(recipient.getId(), NotificationType.FOLLOW)).thenReturn(true);
            when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));

            Notification savedNotification = new Notification(
                    recipient, null, NotificationType.FOLLOW, null, "System notification");
            savedNotification.setId(UUID.randomUUID());
            savedNotification.setCreatedAt(LocalDateTime.now());

            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

            // When
            NotificationResponseDto result = notificationService.createNotification(
                    recipient.getId(), null, NotificationType.FOLLOW, null, "System notification");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.senderPseudo()).isNull();
            assertThat(result.senderPicture()).isNull();

            verify(notificationRepository).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("getNotifications")
    class GetNotifications {

        @Test
        @DisplayName("should return paged results without filters")
        void getNotifications_shouldReturnPagedResults() {
            // Given
            when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));

            Notification notification = new Notification(
                    recipient, sender, NotificationType.FOLLOW, null, "senderUser started following you");
            notification.setId(UUID.randomUUID());
            notification.setCreatedAt(LocalDateTime.now());

            Page<Notification> page = new PageImpl<>(
                    List.of(notification), PageRequest.of(0, 20), 1);

            when(notificationRepository.findByRecipientWithFilters(
                    eq(recipient.getId()), isNull(), isNull(), any(Pageable.class))).thenReturn(page);

            // When
            PagedResponseDto<NotificationResponseDto> result = notificationService.getNotifications(
                    "recipient@example.com", 0, 20, null, null);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).senderPseudo()).isEqualTo("senderUser");
            assertThat(result.metadata().totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should forward type and isRead filters to repository")
        void getNotifications_shouldForwardFilters() {
            // Given
            when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));

            Page<Notification> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            when(notificationRepository.findByRecipientWithFilters(
                    eq(recipient.getId()), eq(NotificationType.LIKE_REVIEW), eq(false),
                    any(Pageable.class))).thenReturn(page);

            // When
            PagedResponseDto<NotificationResponseDto> result = notificationService.getNotifications(
                    "recipient@example.com", 0, 20, NotificationType.LIKE_REVIEW, false);

            // Then
            assertThat(result.content()).isEmpty();
            verify(notificationRepository).findByRecipientWithFilters(
                    eq(recipient.getId()), eq(NotificationType.LIKE_REVIEW), eq(false), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCount {

        @Test
        @DisplayName("should return unread count")
        void getUnreadCount_shouldReturnCount() {
            // Given
            when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));
            when(notificationRepository.countByRecipientIdAndIsReadFalse(recipient.getId())).thenReturn(5L);

            // When
            UnreadCountDto result = notificationService.getUnreadCount("recipient@example.com");

            // Then
            assertThat(result.count()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("should mark notification as read")
        void markAsRead_shouldUpdateNotification() {
            // Given
            UUID notificationId = UUID.randomUUID();

            when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));

            Notification notification = new Notification(
                    recipient, sender, NotificationType.FOLLOW, null, "senderUser started following you");
            notification.setId(notificationId);
            notification.setIsRead(false);

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            // When
            notificationService.markAsRead(notificationId, "recipient@example.com");

            // Then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getIsRead()).isTrue();
        }

        @Test
        @DisplayName("should throw when notification not found")
        void markAsRead_shouldThrowWhenNotFound() {
            // Given
            UUID notificationId = UUID.randomUUID();
            when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> notificationService.markAsRead(notificationId, "recipient@example.com"))
                    .isInstanceOf(NotificationNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when notification belongs to another user")
        void markAsRead_shouldThrowWhenNotOwnedByUser() {
            // Given
            UUID notificationId = UUID.randomUUID();
            User otherUser = new User();
            otherUser.setId(UUID.randomUUID());
            otherUser.setEmail("other@example.com");

            when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));

            Notification notification = new Notification(
                    otherUser, sender, NotificationType.FOLLOW, null, "message");
            notification.setId(notificationId);

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            // When / Then
            assertThatThrownBy(() -> notificationService.markAsRead(notificationId, "recipient@example.com"))
                    .isInstanceOf(UnauthorizedNotificationAccessException.class);
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsRead {

        @Test
        @DisplayName("should call repository to mark all as read")
        void markAllAsRead_shouldCallRepository() {
            // Given
            when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));
            when(notificationRepository.markAllAsReadByRecipientId(recipient.getId())).thenReturn(3);

            // When
            notificationService.markAllAsRead("recipient@example.com");

            // Then
            verify(notificationRepository).markAllAsReadByRecipientId(recipient.getId());
        }
    }

    @Nested
    @DisplayName("markAsReadBulk")
    class MarkAsReadBulk {

        @Test
        @DisplayName("should forward IDs and recipient to repository")
        void markAsReadBulk_shouldCallRepository() {
            // Given
            Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());
            when(userRepository.findByEmail("recipient@example.com")).thenReturn(Optional.of(recipient));
            when(notificationRepository.markAsReadByIdsAndRecipientId(ids, recipient.getId())).thenReturn(2);

            // When
            int updated = notificationService.markAsReadBulk(ids, "recipient@example.com");

            // Then
            assertThat(updated).isEqualTo(2);
            verify(notificationRepository).markAsReadByIdsAndRecipientId(ids, recipient.getId());
        }
    }
}
