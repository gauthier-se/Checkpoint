package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a review is not found for a specific play log.
 */
public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(UUID playId) {
        super("No review found for play log with ID: " + playId);
    }
}
