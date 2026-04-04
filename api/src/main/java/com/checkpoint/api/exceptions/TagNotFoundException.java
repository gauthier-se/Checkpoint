package com.checkpoint.api.exceptions;

/**
 * Exception thrown when a tag is not found.
 */
public class TagNotFoundException extends RuntimeException {

    public TagNotFoundException(String message) {
        super(message);
    }
}
