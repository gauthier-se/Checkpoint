package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a user tries to access or modify a list they do not own.
 */
public class UnauthorizedListAccessException extends RuntimeException {

    private final UUID listId;

    public UnauthorizedListAccessException(UUID listId) {
        super("You do not have permission to access list with ID: " + listId);
        this.listId = listId;
    }

    public UUID getListId() {
        return listId;
    }
}
