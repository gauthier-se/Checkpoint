package com.checkpoint.api.security.impl;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.UserBannedException;
import com.checkpoint.api.repositories.UserRepository;

/**
 * Loads user-specific data for Spring Security authentication.
 * Bridges the {@link User} entity with Spring Security's {@link UserDetails}.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        if (Boolean.TRUE.equals(user.getBanned())) {
            throw new UserBannedException(email);
        }

        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";

        // OAuth2 users have a null password; Spring's UserDetails forbids null/empty,
        // so substitute an unmatchable placeholder. Form login still rejects them
        // because this is not a valid BCrypt hash, so BCryptPasswordEncoder.matches
        // always returns false.
        String password = user.getPassword() != null ? user.getPassword() : OAUTH2_PASSWORD_PLACEHOLDER;

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                password,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()))
        );
    }

    private static final String OAUTH2_PASSWORD_PLACEHOLDER = "__oauth2_no_password__";
}
