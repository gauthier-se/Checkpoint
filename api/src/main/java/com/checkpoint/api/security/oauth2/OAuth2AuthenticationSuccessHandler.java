package com.checkpoint.api.security.oauth2;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.checkpoint.api.security.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles successful OAuth2 logins by:
 *
 * <ol>
 *   <li>Resolving the {@link UserDetails} for the email returned by the provider.</li>
 *   <li>Generating a JWT and writing it as a {@code checkpoint_token} HttpOnly cookie.</li>
 *   <li>Redirecting to the configured frontend URL.</li>
 * </ol>
 *
 * No server-side session is created — the flow is fully stateless.
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private static final String COOKIE_NAME = "checkpoint_token";

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final SimpleUrlAuthenticationSuccessHandler delegate;
    private final String fallbackTargetUrl;
    private final boolean cookieSecure;
    private final long jwtExpirationMs;

    public OAuth2AuthenticationSuccessHandler(UserDetailsService userDetailsService,
                                              JwtService jwtService,
                                              @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl,
                                              @Value("${app.cookie.secure:true}") boolean cookieSecure,
                                              @Value("${jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.fallbackTargetUrl = frontendUrl + "/";
        this.delegate = new SimpleUrlAuthenticationSuccessHandler(this.fallbackTargetUrl);
        this.cookieSecure = cookieSecure;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        String email = extractEmail(authentication);
        if (email == null) {
            log.warn("OAuth2 success handler could not extract email from principal");
            response.sendRedirect(fallbackTargetUrl);
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api")
                .maxAge(jwtExpirationMs / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        log.info("OAuth2 login successful for {}", email);
        delegate.onAuthenticationSuccess(request, response, authToken);
    }

    private static String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidc) {
            return oidc.getEmail();
        }
        if (principal instanceof OAuth2User oauth) {
            Object email = oauth.getAttributes().get("email");
            return email != null ? email.toString() : oauth.getName();
        }
        return null;
    }
}
