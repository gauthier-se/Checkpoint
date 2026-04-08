package com.checkpoint.api.dto.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import com.checkpoint.api.enums.NotificationType;

/**
 * DTO for notification responses.
 *
 * @param id            the notification ID
 * @param senderPseudo  the sender's pseudo (null for system notifications)
 * @param senderPicture the sender's profile picture URL (null for system notifications)
 * @param type          the notification type
 * @param referenceId   the related entity ID (nullable)
 * @param message       the pre-formatted notification message
 * @param isRead        whether the notification has been read
 * @param createdAt     when the notification was created
 */
public record NotificationResponseDto(
        UUID id,
        String senderPseudo,
        String senderPicture,
        NotificationType type,
        UUID referenceId,
        String message,
        Boolean isRead,
        LocalDateTime createdAt
) {}
