package com.checkpoint.api.exceptions;

/**
 * Exception thrown when a non-owner attempts to access private profile data.
 */
public class ProfilePrivateException extends RuntimeException {

    public ProfilePrivateException(String username) {
        super("Profile is private: " + username);
    }
}
