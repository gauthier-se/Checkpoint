package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.dto.notification.NotificationResponseDto;
import com.checkpoint.api.dto.notification.UnreadCountDto;
import com.checkpoint.api.enums.NotificationType;

/**
 * Service for managing user notifications.
 */
public interface NotificationService {

    /**
     * Creates a new notification, persists it, and pushes it via WebSocket
     * to the recipient if they are connected.
     *
     * @param recipientId the recipient user's ID
     * @param senderId    the sender user's ID (nullable for system notifications)
     * @param type        the notification type
     * @param referenceId the related entity ID (nullable)
     * @param message     the pre-formatted notification message
     * @return the created notification as a DTO
     */
    NotificationResponseDto createNotification(UUID recipientId, UUID senderId, NotificationType type,
                                               UUID referenceId, String message);

    /**
     * Returns a paginated list of notifications for the authenticated user.
     *
     * @param userEmail the authenticated user's email
     * @param page      the page number (0-based)
     * @param size      the page size
     * @return a paginated response of notification DTOs
     */
    PagedResponseDto<NotificationResponseDto> getNotifications(String userEmail, int page, int size);

    /**
     * Returns the number of unread notifications for the authenticated user.
     *
     * @param userEmail the authenticated user's email
     * @return the unread count DTO
     */
    UnreadCountDto getUnreadCount(String userEmail);

    /**
     * Marks a single notification as read.
     * The notification must belong to the authenticated user.
     *
     * @param notificationId the notification ID
     * @param userEmail      the authenticated user's email
     */
    void markAsRead(UUID notificationId, String userEmail);

    /**
     * Marks all notifications as read for the authenticated user.
     *
     * @param userEmail the authenticated user's email
     */
    void markAllAsRead(String userEmail);
}
