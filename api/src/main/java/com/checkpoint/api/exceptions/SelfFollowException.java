package com.checkpoint.api.exceptions;

/**
 * Exception thrown when a user attempts to follow themselves.
 */
public class SelfFollowException extends RuntimeException {

    public SelfFollowException() {
        super("You cannot follow yourself");
    }
}
