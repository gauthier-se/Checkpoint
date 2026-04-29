package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.entities.AuthProvider;
import com.checkpoint.api.entities.Role;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.UserBannedException;
import com.checkpoint.api.repositories.RoleRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.impl.OAuth2UserServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2UserServiceImpl")
class OAuth2UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private OAuth2UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OAuth2UserServiceImpl(userRepository, roleRepository);
    }

    @Test
    @DisplayName("loadOrCreateUser_shouldReturnExistingUserWhenFoundByProviderAndId")
    void loadOrCreateUser_shouldReturnExistingUserWhenFoundByProviderAndId() {
        // Given
        User existing = new User("alice", "alice@example.com", AuthProvider.GOOGLE, "g-123");
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.of(existing));

        // When
        User result = service.loadOrCreateUser(AuthProvider.GOOGLE, "g-123",
                "alice@example.com", "Alice", null);

        // Then
        assertThat(result).isSameAs(existing);
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("loadOrCreateUser_shouldLinkProviderToExistingLocalAccountWithSameEmail")
    void loadOrCreateUser_shouldLinkProviderToExistingLocalAccountWithSameEmail() {
        // Given
        User local = new User("bob", "bob@example.com", "encodedPassword");
        local.setProvider(AuthProvider.LOCAL);
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "g-456"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(local));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.loadOrCreateUser(AuthProvider.GOOGLE, "g-456",
                "bob@example.com", "Bob", "https://avatar/b.png");

        // Then
        assertThat(result.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(result.getProviderId()).isEqualTo("g-456");
        assertThat(result.getPicture()).isEqualTo("https://avatar/b.png");
        verify(userRepository, times(1)).save(local);
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    @DisplayName("loadOrCreateUser_shouldKeepExistingPictureWhenLinking")
    void loadOrCreateUser_shouldKeepExistingPictureWhenLinking() {
        // Given
        User local = new User("carol", "carol@example.com", "encodedPassword");
        local.setPicture("https://avatar/existing.png");
        when(userRepository.findByProviderAndProviderId(AuthProvider.TWITCH, "t-789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(local));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.loadOrCreateUser(AuthProvider.TWITCH, "t-789",
                "carol@example.com", "carol_streams", "https://avatar/twitch.png");

        // Then
        assertThat(result.getPicture()).isEqualTo("https://avatar/existing.png");
    }

    @Test
    @DisplayName("loadOrCreateUser_shouldCreateNewUserWithUniquePseudoWhenNotFound")
    void loadOrCreateUser_shouldCreateNewUserWithUniquePseudoWhenNotFound() {
        // Given
        Role userRole = new Role("USER");
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "g-new"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.existsByPseudo("dave")).thenReturn(true);
        when(userRepository.existsByPseudo("dave2")).thenReturn(true);
        when(userRepository.existsByPseudo("dave3")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.loadOrCreateUser(AuthProvider.GOOGLE, "g-new",
                "new@example.com", "dave", "https://avatar/d.png");

        // Then
        assertThat(result.getPseudo()).isEqualTo("dave3");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(result.getProviderId()).isEqualTo("g-new");
        assertThat(result.getPassword()).isNull();
        assertThat(result.getPicture()).isEqualTo("https://avatar/d.png");
        assertThat(result.getRole()).isSameAs(userRole);
    }

    @Test
    @DisplayName("loadOrCreateUser_shouldFallBackToEmailLocalPartWhenNameMissing")
    void loadOrCreateUser_shouldFallBackToEmailLocalPartWhenNameMissing() {
        // Given
        when(userRepository.findByProviderAndProviderId(any(), any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("erin.smith@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(new Role("USER")));
        when(userRepository.existsByPseudo("erinsmith")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.loadOrCreateUser(AuthProvider.TWITCH, "t-erin",
                "erin.smith@example.com", null, null);

        // Then
        assertThat(result.getPseudo()).isEqualTo("erinsmith");
    }

    @Test
    @DisplayName("loadOrCreateUser_shouldRejectBannedExistingUser")
    void loadOrCreateUser_shouldRejectBannedExistingUser() {
        // Given
        User banned = new User("frank", "frank@example.com", AuthProvider.GOOGLE, "g-banned");
        banned.setBanned(true);
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "g-banned"))
                .thenReturn(Optional.of(banned));

        // When / Then
        assertThatThrownBy(() -> service.loadOrCreateUser(AuthProvider.GOOGLE, "g-banned",
                "frank@example.com", "Frank", null))
                .isInstanceOf(UserBannedException.class);
    }

    @Test
    @DisplayName("loadOrCreateUser_shouldRejectMissingEmail")
    void loadOrCreateUser_shouldRejectMissingEmail() {
        // When / Then
        assertThatThrownBy(() -> service.loadOrCreateUser(AuthProvider.GOOGLE, "g-x", "", "name", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
