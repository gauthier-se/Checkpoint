package com.checkpoint.api.security.oauth2;

import java.io.IOException;

import jakarta.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles successful OAuth2 logins by:
 *
 * <ol>
 *   <li>Resolving the {@link UserDetails} for the email returned by the provider
 *       (carried by the OAuth2 principal's {@code name}).</li>
 *   <li>Replacing the OAuth2 authentication in the security context with the
 *       same {@link UsernamePasswordAuthenticationToken} produced by form login,
 *       so the rest of the application sees a uniform principal type.</li>
 *   <li>Persisting the security context to the HTTP session, then redirecting
 *       to the configured frontend URL.</li>
 * </ol>
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final UserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;
    private final SimpleUrlAuthenticationSuccessHandler delegate;
    private final String fallbackTargetUrl;

    public OAuth2AuthenticationSuccessHandler(UserDetailsService userDetailsService,
                                              @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = new DelegatingSecurityContextRepository(
                new HttpSessionSecurityContextRepository(),
                new RequestAttributeSecurityContextRepository());
        this.fallbackTargetUrl = frontendUrl + "/";
        this.delegate = new SimpleUrlAuthenticationSuccessHandler(this.fallbackTargetUrl);
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
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        // Persist the rewritten context so the next request reads back a
        // UsernamePasswordAuthenticationToken (with UserDetails as principal),
        // not the original OAuth2AuthenticationToken stored by the OAuth2 filter.
        securityContextRepository.saveContext(context, request, response);

        log.info("OAuth2 login successful for {}", email);
        delegate.onAuthenticationSuccess(request, response, token);
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
