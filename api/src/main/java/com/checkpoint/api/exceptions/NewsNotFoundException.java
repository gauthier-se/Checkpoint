package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a news article is not found.
 */
public class NewsNotFoundException extends RuntimeException {

    public NewsNotFoundException(UUID newsId) {
        super("News not found with ID: " + newsId);
    }

    public NewsNotFoundException(String message) {
        super(message);
    }
}
