package com.checkpoint.api.services;

import com.checkpoint.api.entities.AuthProvider;
import com.checkpoint.api.entities.User;

/**
 * Service responsible for resolving the local {@link User} corresponding to
 * an OAuth2 authentication.
 *
 * <p>Performs three concerns in one place: lookup by provider identifier,
 * automatic account linking when a provider returns an email already used
 * by an existing local account, and creation of a brand-new account on
 * first login through the given provider.</p>
 */
public interface OAuth2UserService {

    /**
     * Returns the local user matching the given OAuth2 identity, creating or
     * linking the account on the fly when needed.
     *
     * @param provider   the OAuth2 provider
     * @param providerId the provider-specific user identifier (must not be null)
     * @param email      the verified email address returned by the provider
     * @param name       the display name returned by the provider, used as a hint
     *                   for pseudo generation when creating a new account
     * @param picture    the avatar URL returned by the provider; may be null
     * @return the persisted local user
     */
    User loadOrCreateUser(AuthProvider provider, String providerId, String email,
                          String name, String picture);
}
