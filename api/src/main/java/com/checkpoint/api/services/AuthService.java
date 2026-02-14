package com.checkpoint.api.services;

import com.checkpoint.api.dto.auth.LoginRequestDto;

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
}
