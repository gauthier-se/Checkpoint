package com.checkpoint.api.security.oauth2;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.checkpoint.api.entities.AuthProvider;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.services.OAuth2UserService;

/**
 * Spring Security {@link OidcUserService} extension for OIDC providers
 * (currently Google), delegating account upsert/linking to {@link OAuth2UserService}.
 *
 * <p>The returned principal carries the local user's email as its
 * {@link OidcUser#getName()} attribute so the success handler can resolve the
 * corresponding {@link User}.</p>
 */
@Service
public class CheckpointOidcUserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(CheckpointOidcUserService.class);
    private static final String GOOGLE_REGISTRATION_ID = "google";

    private final OAuth2UserService oAuth2UserService;

    public CheckpointOidcUserService(OAuth2UserService oAuth2UserService) {
        this.oAuth2UserService = oAuth2UserService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser upstream = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (!GOOGLE_REGISTRATION_ID.equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                    "Unsupported OIDC provider: " + registrationId);
        }

        String providerId = upstream.getSubject();
        String email = upstream.getEmail();
        String name = upstream.getFullName();
        String picture = upstream.getPicture();

        if (providerId == null || email == null) {
            log.warn("Google OIDC response missing sub or email");
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"),
                    "Google did not return the required sub and email claims");
        }

        User user = oAuth2UserService.loadOrCreateUser(
                AuthProvider.GOOGLE, providerId, email, name, picture);

        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        return new DefaultOidcUser(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase())),
                upstream.getIdToken(),
                upstream.getUserInfo(),
                "email");
    }
}
