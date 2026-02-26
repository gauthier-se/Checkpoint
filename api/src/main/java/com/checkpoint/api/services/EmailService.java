package com.checkpoint.api.services;

/**
 * Interface for email operations.
 */
public interface EmailService {

    /**
     * Sends a password reset email to the specified user.
     *
     * @param to        the recipient's email address
     * @param resetLink the link to reset the password
     */
    void sendPasswordResetEmail(String to, String resetLink);
}
