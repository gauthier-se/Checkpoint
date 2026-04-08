package com.checkpoint.api.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * STOMP channel interceptor that authenticates WebSocket CONNECT frames
 * using JWT tokens.
 *
 * <p>Supports two methods of providing the JWT:</p>
 * <ul>
 *   <li>STOMP native header {@code Authorization: Bearer &lt;token&gt;} (preferred)</li>
 *   <li>STOMP native header {@code token: &lt;token&gt;} (SockJS fallback)</li>
 * </ul>
 *
 * <p>Unauthenticated connections are rejected by not setting a principal,
 * which prevents subscription to user-specific destinations.</p>
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Intercepts STOMP CONNECT frames to authenticate the user via JWT.
     *
     * @param message the incoming message
     * @param channel the message channel
     * @return the message (potentially with a Principal set)
     */
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwt = extractJwt(accessor);

            if (jwt != null) {
                try {
                    String username = jwtService.extractUsername(jwt);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );
                            accessor.setUser(authToken);
                            log.info("WebSocket CONNECT authenticated for user: {}", username);
                        } else {
                            log.warn("WebSocket CONNECT rejected: invalid JWT for user {}", username);
                        }
                    }
                } catch (Exception ex) {
                    log.warn("WebSocket CONNECT authentication failed: {}", ex.getMessage());
                }
            } else {
                log.warn("WebSocket CONNECT rejected: no JWT token provided");
            }
        }

        return message;
    }

    /**
     * Extracts the JWT token from STOMP native headers.
     * Tries the Authorization header first, then falls back to the token header.
     *
     * @param accessor the STOMP header accessor
     * @return the JWT token string, or null if not found
     */
    private String extractJwt(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith(BEARER_PREFIX)) {
                return authHeader.substring(BEARER_PREFIX.length());
            }
        }

        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }

        return null;
    }
}
