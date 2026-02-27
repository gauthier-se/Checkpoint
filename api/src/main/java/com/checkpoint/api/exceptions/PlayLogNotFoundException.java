package com.checkpoint.api.exceptions;

/**
 * Exception thrown when a play log is not found or does not belong to the user.
 */
public class PlayLogNotFoundException extends RuntimeException {

    public PlayLogNotFoundException(String message) {
        super(message);
    }
}
