package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.checkpoint.api.dto.collection.BacklogResponseDto;
import com.checkpoint.api.entities.Backlog;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.enums.Priority;
import com.checkpoint.api.exceptions.GameAlreadyInBacklogException;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.GameNotInBacklogException;
import com.checkpoint.api.mapper.BacklogMapper;
import com.checkpoint.api.repositories.BacklogRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.impl.BacklogServiceImpl;

/**
 * Unit tests for {@link BacklogServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class BacklogServiceImplTest {

    @Mock
    private BacklogRepository backlogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoGameRepository videoGameRepository;

    @Mock
    private BacklogMapper backlogMapper;

    private BacklogServiceImpl service;

    private User testUser;
    private VideoGame testGame;
    private Backlog testBacklog;
    private BacklogResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        service = new BacklogServiceImpl(
                backlogRepository, userRepository, videoGameRepository, backlogMapper);

        testUser = new User("testuser", "user@example.com", "password");
        testUser.setId(UUID.randomUUID());

        testGame = new VideoGame("The Witcher 3", "Epic RPG", LocalDate.of(2015, 5, 19));
        testGame.setId(UUID.randomUUID());
        testGame.setCoverUrl("cover.jpg");

        testBacklog = new Backlog(testUser, testGame);
        testBacklog.setId(UUID.randomUUID());
        testBacklog.setCreatedAt(LocalDateTime.now());
        testBacklog.setUpdatedAt(LocalDateTime.now());

        testResponseDto = new BacklogResponseDto(
                testBacklog.getId(), testGame.getId(), testGame.getTitle(),
                testGame.getCoverUrl(), testGame.getReleaseDate(),
                null, testBacklog.getCreatedAt());
    }

    @Nested
    @DisplayName("addToBacklog")
    class AddToBacklog {

        @Test
        @DisplayName("should add game to backlog successfully")
        void shouldAddGameSuccessfully() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(testGame.getId())).thenReturn(Optional.of(testGame));
            when(backlogRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId())).thenReturn(false);
            when(backlogRepository.save(any(Backlog.class))).thenReturn(testBacklog);
            when(backlogMapper.toResponseDto(testBacklog)).thenReturn(testResponseDto);

            // When
            BacklogResponseDto result = service.addToBacklog("user@example.com", testGame.getId(), null);

            // Then
            assertThat(result.videoGameId()).isEqualTo(testGame.getId());
            assertThat(result.title()).isEqualTo("The Witcher 3");

            ArgumentCaptor<Backlog> captor = ArgumentCaptor.forClass(Backlog.class);
            verify(backlogRepository).save(captor.capture());
            assertThat(captor.getValue().getUser()).isEqualTo(testUser);
            assertThat(captor.getValue().getVideoGame()).isEqualTo(testGame);
        }

        @Test
        @DisplayName("should add game with given priority")
        void shouldAddGameWithPriority() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(testGame.getId())).thenReturn(Optional.of(testGame));
            when(backlogRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId())).thenReturn(false);
            when(backlogRepository.save(any(Backlog.class))).thenReturn(testBacklog);
            when(backlogMapper.toResponseDto(testBacklog)).thenReturn(testResponseDto);

            // When
            service.addToBacklog("user@example.com", testGame.getId(), Priority.HIGH);

            // Then
            ArgumentCaptor<Backlog> captor = ArgumentCaptor.forClass(Backlog.class);
            verify(backlogRepository).save(captor.capture());
            assertThat(captor.getValue().getPriority()).isEqualTo(Priority.HIGH);
        }

        @Test
        @DisplayName("should throw GameAlreadyInBacklogException when game already in backlog")
        void shouldThrowWhenGameAlreadyInBacklog() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(testGame.getId())).thenReturn(Optional.of(testGame));
            when(backlogRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId())).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> service.addToBacklog("user@example.com", testGame.getId(), null))
                    .isInstanceOf(GameAlreadyInBacklogException.class)
                    .hasMessageContaining(testGame.getId().toString());

            verify(backlogRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw GameNotFoundException when video game does not exist")
        void shouldThrowWhenVideoGameNotFound() {
            // Given
            UUID unknownGameId = UUID.randomUUID();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(unknownGameId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.addToBacklog("user@example.com", unknownGameId, null))
                    .isInstanceOf(GameNotFoundException.class)
                    .hasMessageContaining(unknownGameId.toString());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when user not found")
        void shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.addToBacklog("unknown@example.com", testGame.getId(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unknown@example.com");
        }
    }

    @Nested
    @DisplayName("removeFromBacklog")
    class RemoveFromBacklog {

        @Test
        @DisplayName("should remove game from backlog successfully")
        void shouldRemoveGameSuccessfully() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId()))
                    .thenReturn(true);

            // When
            service.removeFromBacklog("user@example.com", testGame.getId());

            // Then
            verify(backlogRepository).deleteByUserIdAndVideoGameId(testUser.getId(), testGame.getId());
        }

        @Test
        @DisplayName("should throw GameNotInBacklogException when game not in backlog")
        void shouldThrowWhenGameNotInBacklog() {
            // Given
            UUID videoGameId = UUID.randomUUID();
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.existsByUserIdAndVideoGameId(testUser.getId(), videoGameId))
                    .thenReturn(false);

            // When / Then
            assertThatThrownBy(() -> service.removeFromBacklog("user@example.com", videoGameId))
                    .isInstanceOf(GameNotInBacklogException.class)
                    .hasMessageContaining(videoGameId.toString());

            verify(backlogRepository, never()).deleteByUserIdAndVideoGameId(any(), any());
        }
    }

    @Nested
    @DisplayName("getUserBacklog")
    class GetUserBacklog {

        @Test
        @DisplayName("should return paginated user backlog")
        void shouldReturnPaginatedBacklog() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Backlog> backlogPage = new PageImpl<>(List.of(testBacklog), pageable, 1);

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.findByUserIdWithVideoGame(testUser.getId(), pageable))
                    .thenReturn(backlogPage);
            when(backlogMapper.toResponseDto(testBacklog)).thenReturn(testResponseDto);

            // When
            Page<BacklogResponseDto> result = service.getUserBacklog("user@example.com", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("The Witcher 3");
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty page when backlog is empty")
        void shouldReturnEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Backlog> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.findByUserIdWithVideoGame(testUser.getId(), pageable))
                    .thenReturn(emptyPage);

            // When
            Page<BacklogResponseDto> result = service.getUserBacklog("user@example.com", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getUserBacklog with priority sort")
    class GetUserBacklogByPriority {

        @Test
        @DisplayName("should call priority-desc repository when sort field is priority,desc")
        void shouldUsePriorityDescQuery_whenSortIsPriorityDesc() {
            // Given
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "priority"));
            Page<Backlog> backlogPage = new PageImpl<>(List.of(testBacklog));

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.findByUserIdWithVideoGameOrderByPriorityDesc(eq(testUser.getId()), any(Pageable.class)))
                    .thenReturn(backlogPage);
            when(backlogMapper.toResponseDto(testBacklog)).thenReturn(testResponseDto);

            // When
            Page<BacklogResponseDto> result = service.getUserBacklog("user@example.com", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(backlogRepository).findByUserIdWithVideoGameOrderByPriorityDesc(eq(testUser.getId()), any(Pageable.class));
            verify(backlogRepository, never()).findByUserIdWithVideoGame(any(), any());
        }

        @Test
        @DisplayName("should call priority-asc repository when sort field is priority,asc")
        void shouldUsePriorityAscQuery_whenSortIsPriorityAsc() {
            // Given
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "priority"));
            Page<Backlog> backlogPage = new PageImpl<>(List.of(testBacklog));

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.findByUserIdWithVideoGameOrderByPriorityAsc(eq(testUser.getId()), any(Pageable.class)))
                    .thenReturn(backlogPage);
            when(backlogMapper.toResponseDto(testBacklog)).thenReturn(testResponseDto);

            // When
            Page<BacklogResponseDto> result = service.getUserBacklog("user@example.com", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(backlogRepository).findByUserIdWithVideoGameOrderByPriorityAsc(eq(testUser.getId()), any(Pageable.class));
            verify(backlogRepository, never()).findByUserIdWithVideoGame(any(), any());
        }
    }

    @Nested
    @DisplayName("updatePriority")
    class UpdatePriority {

        @Test
        @DisplayName("should update and return DTO when backlog entry exists")
        void shouldUpdateAndReturnDto() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.findByUserIdAndVideoGameId(testUser.getId(), testGame.getId()))
                    .thenReturn(Optional.of(testBacklog));
            when(backlogRepository.save(testBacklog)).thenReturn(testBacklog);
            when(backlogMapper.toResponseDto(testBacklog)).thenReturn(testResponseDto);

            // When
            BacklogResponseDto result = service.updatePriority("user@example.com", testGame.getId(), Priority.HIGH);

            // Then
            assertThat(result).isEqualTo(testResponseDto);
            assertThat(testBacklog.getPriority()).isEqualTo(Priority.HIGH);
            verify(backlogRepository).save(testBacklog);
        }

        @Test
        @DisplayName("should clear priority when null is passed")
        void shouldClearPriority_whenNullPassed() {
            // Given
            testBacklog.setPriority(Priority.HIGH);
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.findByUserIdAndVideoGameId(testUser.getId(), testGame.getId()))
                    .thenReturn(Optional.of(testBacklog));
            when(backlogRepository.save(testBacklog)).thenReturn(testBacklog);
            when(backlogMapper.toResponseDto(testBacklog)).thenReturn(testResponseDto);

            // When
            service.updatePriority("user@example.com", testGame.getId(), null);

            // Then
            assertThat(testBacklog.getPriority()).isNull();
            verify(backlogRepository).save(testBacklog);
        }

        @Test
        @DisplayName("should throw GameNotInBacklogException when backlog entry not found")
        void shouldThrow_whenBacklogNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.findByUserIdAndVideoGameId(testUser.getId(), unknownId))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.updatePriority("user@example.com", unknownId, Priority.LOW))
                    .isInstanceOf(GameNotInBacklogException.class)
                    .hasMessageContaining(unknownId.toString());

            verify(backlogRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("isInBacklog")
    class IsInBacklog {

        @Test
        @DisplayName("should return true when game is in backlog")
        void shouldReturnTrueWhenInBacklog() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId()))
                    .thenReturn(true);

            // When
            boolean result = service.isInBacklog("user@example.com", testGame.getId());

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when game is not in backlog")
        void shouldReturnFalseWhenNotInBacklog() {
            // Given
            UUID videoGameId = UUID.randomUUID();
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(backlogRepository.existsByUserIdAndVideoGameId(testUser.getId(), videoGameId))
                    .thenReturn(false);

            // When
            boolean result = service.isInBacklog("user@example.com", videoGameId);

            // Then
            assertThat(result).isFalse();
        }
    }
}
