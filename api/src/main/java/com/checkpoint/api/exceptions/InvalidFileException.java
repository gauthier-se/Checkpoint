package com.checkpoint.api.exceptions;

/**
 * Exception thrown when an uploaded file is invalid
 * (wrong type, too large, or empty).
 */
public class InvalidFileException extends RuntimeException {

    /**
     * Constructs a new InvalidFileException.
     *
     * @param message the detail message
     */
    public InvalidFileException(String message) {
        super(message);
    }
}
