package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a user attempts to report a review they have already reported.
 */
public class DuplicateReportException extends RuntimeException {

    public DuplicateReportException(UUID reviewId) {
        super("User has already reported review with ID: " + reviewId);
    }
}
