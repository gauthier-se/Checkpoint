package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.dto.admin.AdminUserDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.impl.AdminUserServiceImpl;

/**
 * Unit tests for {@link AdminUserServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(userRepository);
    }

    private User createUser(UUID id, String pseudo, String email) {
        User user = new User();
        user.setId(id);
        user.setPseudo(pseudo);
        user.setEmail(email);
        return user;
    }

    @Test
    @DisplayName("getAllUsers should return mapped DTOs for all users")
    void getAllUsers_shouldReturnMappedDtos() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        List<User> users = List.of(
                createUser(id1, "alice", "alice@example.com"),
                createUser(id2, "bob", "bob@example.com"),
                createUser(id3, "charlie", "charlie@example.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<AdminUserDto> result = adminUserService.getAllUsers();

        // Then
        assertThat(result).hasSize(3);

        assertThat(result.get(0).id()).isEqualTo(id1);
        assertThat(result.get(0).username()).isEqualTo("alice");
        assertThat(result.get(0).email()).isEqualTo("alice@example.com");

        assertThat(result.get(1).id()).isEqualTo(id2);
        assertThat(result.get(1).username()).isEqualTo("bob");
        assertThat(result.get(1).email()).isEqualTo("bob@example.com");

        assertThat(result.get(2).id()).isEqualTo(id3);
        assertThat(result.get(2).username()).isEqualTo("charlie");
        assertThat(result.get(2).email()).isEqualTo("charlie@example.com");

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers should return empty list when no users exist")
    void getAllUsers_shouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<AdminUserDto> result = adminUserService.getAllUsers();

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers should map pseudo field to username in DTO")
    void getAllUsers_shouldMapPseudoToUsername() {
        // Given
        UUID id = UUID.randomUUID();
        User user = createUser(id, "myPseudo", "user@example.com");
        when(userRepository.findAll()).thenReturn(List.of(user));

        // When
        List<AdminUserDto> result = adminUserService.getAllUsers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().username()).isEqualTo("myPseudo");
        assertThat(result.getFirst().id()).isEqualTo(id);
        assertThat(result.getFirst().email()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("getAllUsers should preserve UUID values")
    void getAllUsers_shouldPreserveUuids() {
        // Given
        UUID specificId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        User user = createUser(specificId, "testuser", "test@example.com");
        when(userRepository.findAll()).thenReturn(List.of(user));

        // When
        List<AdminUserDto> result = adminUserService.getAllUsers();

        // Then
        assertThat(result.getFirst().id())
                .isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    }
}
