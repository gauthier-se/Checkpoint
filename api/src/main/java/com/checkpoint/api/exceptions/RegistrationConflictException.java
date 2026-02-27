package com.checkpoint.api.exceptions;

/**
 * Exception thrown when a registration attempt conflicts with existing data
 * (e.g., duplicate email or pseudo).
 */
public class RegistrationConflictException extends RuntimeException {

    public RegistrationConflictException(String message) {
        super(message);
    }
}
