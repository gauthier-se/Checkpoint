package com.checkpoint.api.services;

import org.springframework.security.core.userdetails.UserDetails;

import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.UserMeDto;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Service interface for authentication operations.
 * Supports hybrid authentication: JWT in HttpOnly cookie for Web, JWT in Authorization header for Desktop.
 */
public interface AuthService {

    /**
     * Authenticates a user and returns a JWT token (for Desktop clients).
     *
     * @param request the login credentials
     * @return the generated JWT token
     */
    String authenticateAndGenerateToken(LoginRequestDto request);

    /**
     * Authenticates a user and writes a {@code checkpoint_token} HttpOnly cookie (for Web clients).
     *
     * @param request         the login credentials
     * @param servletResponse the HTTP servlet response to write the cookie on
     */
    void authenticateAndSetCookie(LoginRequestDto request, HttpServletResponse servletResponse);

    /**
     * Clears the {@code checkpoint_token} cookie by setting {@code Max-Age=0} (for Web logout).
     *
     * @param servletResponse the HTTP servlet response to write the expired cookie on
     */
    void clearAuthCookie(HttpServletResponse servletResponse);

    /**
     * Retrieves profile information for the currently authenticated user.
     *
     * @param email the authenticated user's email
     * @return user profile information
     */
    UserMeDto getCurrentUser(String email);

    /**
     * Registers a new user account.
     *
     * @param request the registration details
     */
    void register(com.checkpoint.api.dto.auth.RegisterRequestDto request);

    /**
     * Handles forgot password requests by generating a token.
     *
     * @param request the forgot password request containing the email
     */
    void forgotPassword(com.checkpoint.api.dto.auth.ForgotPasswordRequestDto request);

    /**
     * Resets the user's password using the provided token.
     *
     * @param request the reset password request containing the token and new password
     */
    void resetPassword(com.checkpoint.api.dto.auth.ResetPasswordRequestDto request);

    /**
     * Generates a short-lived JWT for WebSocket authentication.
     *
     * @param userDetails the authenticated user principal
     * @return a JWT token string
     */
    String generateWsToken(UserDetails userDetails);
}
