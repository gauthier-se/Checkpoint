package com.checkpoint.api.exceptions;

/**
 * Exception thrown when an external game is not found on the external API.
 */
public class ExternalGameNotFoundException extends RuntimeException {

    private final Long externalId;

    public ExternalGameNotFoundException(Long externalId) {
        super("External game not found with ID: " + externalId);
        this.externalId = externalId;
    }

    public Long getExternalId() {
        return externalId;
    }
}
