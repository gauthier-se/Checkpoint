package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a review is not found.
 */
public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(UUID playId) {
        super("No review found for play log with ID: " + playId);
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }
}
