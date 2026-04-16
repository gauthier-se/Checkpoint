package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a report is not found.
 */
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(UUID reportId) {
        super("Report not found with ID: " + reportId);
    }
}
