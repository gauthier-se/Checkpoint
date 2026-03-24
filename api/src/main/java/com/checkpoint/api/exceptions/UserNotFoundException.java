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

    /**
     * Constructs a new UserNotFoundException for a lookup by username.
     *
     * @param username the username that was not found
     */
    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
        this.userId = null;
    }

    public UUID getUserId() {
        return userId;
    }
}
