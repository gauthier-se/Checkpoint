package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.dto.admin.AdminUserDetailDto;
import com.checkpoint.api.dto.admin.AdminUserDto;
import com.checkpoint.api.dto.admin.AdminUserEditDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.UserNotFoundException;
import com.checkpoint.api.repositories.ReportRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.impl.AdminUserServiceImpl;

/**
 * Unit tests for {@link AdminUserServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReportRepository reportRepository;

    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(userRepository, reviewRepository, reportRepository);
    }

    private User createUser(UUID id, String pseudo, String email) {
        User user = new User();
        user.setId(id);
        user.setPseudo(pseudo);
        user.setEmail(email);
        user.setBanned(false);
        user.setIsPrivate(false);
        user.setXpPoint(100);
        user.setLevel(5);
        user.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        return user;
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return mapped DTOs for all users")
        void getAllUsers_shouldReturnMappedDtos() {
            // Given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<User> users = List.of(
                    createUser(id1, "alice", "alice@example.com"),
                    createUser(id2, "bob", "bob@example.com")
            );
            when(userRepository.findAll()).thenReturn(users);

            // When
            List<AdminUserDto> result = adminUserService.getAllUsers();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(id1);
            assertThat(result.get(0).username()).isEqualTo("alice");
            assertThat(result.get(0).email()).isEqualTo("alice@example.com");
            assertThat(result.get(0).banned()).isFalse();

            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
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
        @DisplayName("Should include banned status in DTO")
        void getAllUsers_shouldIncludeBannedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            User user = createUser(id, "banned_user", "banned@example.com");
            user.setBanned(true);
            when(userRepository.findAll()).thenReturn(List.of(user));

            // When
            List<AdminUserDto> result = adminUserService.getAllUsers();

            // Then
            assertThat(result.getFirst().banned()).isTrue();
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return detailed user DTO with stats")
        void getUserById_shouldReturnDetailDto() {
            // Given
            UUID id = UUID.randomUUID();
            User user = createUser(id, "alice", "alice@example.com");
            user.setBio("A gamer");
            user.setPicture("pic.jpg");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(reviewRepository.countByUserId(id)).thenReturn(10L);
            when(reportRepository.countReportsAgainstUser(id)).thenReturn(2L);

            // When
            AdminUserDetailDto result = adminUserService.getUserById(id);

            // Then
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.username()).isEqualTo("alice");
            assertThat(result.email()).isEqualTo("alice@example.com");
            assertThat(result.bio()).isEqualTo("A gamer");
            assertThat(result.picture()).isEqualTo("pic.jpg");
            assertThat(result.banned()).isFalse();
            assertThat(result.xpPoint()).isEqualTo(100);
            assertThat(result.level()).isEqualTo(5);
            assertThat(result.reviewCount()).isEqualTo(10L);
            assertThat(result.reportCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void getUserById_shouldThrowWhenNotFound() {
            // Given
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminUserService.getUserById(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("editUser")
    class EditUserTests {

        @Test
        @DisplayName("Should clear bio when clearBio is true")
        void editUser_shouldClearBio() {
            // Given
            UUID id = UUID.randomUUID();
            User user = createUser(id, "alice", "alice@example.com");
            user.setBio("Old bio");

            AdminUserEditDto dto = new AdminUserEditDto(true, false, null);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(reviewRepository.countByUserId(id)).thenReturn(0L);
            when(reportRepository.countReportsAgainstUser(id)).thenReturn(0L);

            // When
            AdminUserDetailDto result = adminUserService.editUser(id, dto);

            // Then
            assertThat(result.bio()).isNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should clear picture when clearPicture is true")
        void editUser_shouldClearPicture() {
            // Given
            UUID id = UUID.randomUUID();
            User user = createUser(id, "alice", "alice@example.com");
            user.setPicture("old-pic.jpg");

            AdminUserEditDto dto = new AdminUserEditDto(false, true, null);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(reviewRepository.countByUserId(id)).thenReturn(0L);
            when(reportRepository.countReportsAgainstUser(id)).thenReturn(0L);

            // When
            AdminUserDetailDto result = adminUserService.editUser(id, dto);

            // Then
            assertThat(result.picture()).isNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should toggle isPrivate when provided")
        void editUser_shouldTogglePrivate() {
            // Given
            UUID id = UUID.randomUUID();
            User user = createUser(id, "alice", "alice@example.com");
            user.setIsPrivate(false);

            AdminUserEditDto dto = new AdminUserEditDto(false, false, true);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(reviewRepository.countByUserId(id)).thenReturn(0L);
            when(reportRepository.countReportsAgainstUser(id)).thenReturn(0L);

            // When
            AdminUserDetailDto result = adminUserService.editUser(id, dto);

            // Then
            assertThat(result.isPrivate()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void editUser_shouldThrowWhenNotFound() {
            // Given
            UUID id = UUID.randomUUID();
            AdminUserEditDto dto = new AdminUserEditDto(true, false, null);
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminUserService.editUser(id, dto))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("banUser")
    class BanUserTests {

        @Test
        @DisplayName("Should set banned to true")
        void banUser_shouldSetBannedTrue() {
            // Given
            UUID id = UUID.randomUUID();
            User user = createUser(id, "alice", "alice@example.com");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            adminUserService.banUser(id);

            // Then
            assertThat(user.getBanned()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void banUser_shouldThrowWhenNotFound() {
            // Given
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminUserService.banUser(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("unbanUser")
    class UnbanUserTests {

        @Test
        @DisplayName("Should set banned to false")
        void unbanUser_shouldSetBannedFalse() {
            // Given
            UUID id = UUID.randomUUID();
            User user = createUser(id, "alice", "alice@example.com");
            user.setBanned(true);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            adminUserService.unbanUser(id);

            // Then
            assertThat(user.getBanned()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void unbanUser_shouldThrowWhenNotFound() {
            // Given
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminUserService.unbanUser(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
