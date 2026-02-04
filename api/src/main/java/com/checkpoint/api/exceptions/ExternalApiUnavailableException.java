package com.checkpoint.api.exceptions;

/**
 * Exception thrown when the external API (IGDB) is unavailable.
 */
public class ExternalApiUnavailableException extends RuntimeException {

    public ExternalApiUnavailableException(String message) {
        super(message);
    }

    public ExternalApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
