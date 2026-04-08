package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a user tries to access or modify a notification they do not own.
 */
public class UnauthorizedNotificationAccessException extends RuntimeException {

    public UnauthorizedNotificationAccessException(UUID notificationId) {
        super("You do not have permission to access notification with ID: " + notificationId);
    }
}
