package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a play log already has a review attached.
 */
public class ReviewAlreadyExistsException extends RuntimeException {

    public ReviewAlreadyExistsException(UUID playId) {
        super("A review already exists for play log with ID: " + playId);
    }
}
