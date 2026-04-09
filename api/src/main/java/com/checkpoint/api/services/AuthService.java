package com.checkpoint.api.services;

import org.springframework.security.core.userdetails.UserDetails;

import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.UserMeDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Service interface for authentication operations.
 * Supports hybrid authentication: JWT for Desktop, session for Web.
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
     * Authenticates a user and creates a server-side session (for Web clients).
     *
     * @param request         the login credentials
     * @param servletRequest  the HTTP servlet request (to create session)
     * @param servletResponse the HTTP servlet response
     */
    void authenticateAndCreateSession(LoginRequestDto request,
                                      HttpServletRequest servletRequest,
                                      HttpServletResponse servletResponse);

    /**
     * Logs out a web user by invalidating the session.
     *
     * @param servletRequest  the HTTP servlet request
     * @param servletResponse the HTTP servlet response
     */
    void logoutSession(HttpServletRequest servletRequest,
                       HttpServletResponse servletResponse);

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
     * <p>Used by web clients that are session-authenticated to obtain a token
     * for the STOMP WebSocket connection.</p>
     *
     * @param userDetails the authenticated user principal
     * @return a JWT token string
     */
    String generateWsToken(UserDetails userDetails);
}
