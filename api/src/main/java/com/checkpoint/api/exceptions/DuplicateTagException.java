package com.checkpoint.api.exceptions;

/**
 * Exception thrown when a user tries to create a tag with a name that already exists.
 */
public class DuplicateTagException extends RuntimeException {

    public DuplicateTagException(String message) {
        super(message);
    }
}
