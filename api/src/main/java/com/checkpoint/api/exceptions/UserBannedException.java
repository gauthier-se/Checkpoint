package com.checkpoint.api.exceptions;

/**
 * Exception thrown when a banned user attempts to authenticate.
 */
public class UserBannedException extends RuntimeException {

    public UserBannedException(String email) {
        super("User account is banned: " + email);
    }
}
