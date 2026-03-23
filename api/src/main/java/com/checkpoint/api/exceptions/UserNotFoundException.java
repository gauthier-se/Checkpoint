package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a user is not found in the database.
 */
public class UserNotFoundException extends RuntimeException {

    private final UUID userId;

    public UserNotFoundException(UUID userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
