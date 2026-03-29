package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game list is not found.
 */
public class GameListNotFoundException extends RuntimeException {

    private final UUID listId;

    public GameListNotFoundException(UUID listId) {
        super("Game list not found with ID: " + listId);
        this.listId = listId;
    }

    public UUID getListId() {
        return listId;
    }
}
