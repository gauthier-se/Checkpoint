package com.checkpoint.api.security.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.checkpoint.api.exceptions.UserBannedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Redirects the browser back to the frontend login page with an error code
 * when an OAuth2 authentication fails.
 *
 * <p>Specific Checkpoint exceptions surface as dedicated error codes so the
 * UI can render a meaningful message; everything else is reported as
 * {@code oauth_failed}.</p>
 */
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    private final String frontendUrl;

    public OAuth2AuthenticationFailureHandler(
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorCode = resolveErrorCode(exception);
        log.warn("OAuth2 authentication failed: {} ({})", errorCode, exception.getMessage());
        String target = frontendUrl + "/login?error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
        response.sendRedirect(target);
    }

    private static String resolveErrorCode(AuthenticationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof UserBannedException) {
                return "oauth_banned";
            }
            cause = cause.getCause();
        }
        return "oauth_failed";
    }
}
