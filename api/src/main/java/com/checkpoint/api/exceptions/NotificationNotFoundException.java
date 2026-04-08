package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a notification is not found.
 */
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID notificationId) {
        super("Notification not found with ID: " + notificationId);
    }
}
