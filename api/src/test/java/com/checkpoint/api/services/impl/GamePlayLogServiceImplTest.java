package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.checkpoint.api.dto.playlog.GamePlayLogRequestDto;
import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.entities.Platform;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.PlayLogNotFoundException;
import com.checkpoint.api.mapper.GamePlayLogMapper;
import com.checkpoint.api.repositories.PlatformRepository;
import com.checkpoint.api.repositories.UserGamePlayRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;

@ExtendWith(MockitoExtension.class)
class GamePlayLogServiceImplTest {

    @Mock
    private UserGamePlayRepository userGamePlayRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoGameRepository videoGameRepository;

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private GamePlayLogMapper gamePlayLogMapper;

    @InjectMocks
    private GamePlayLogServiceImpl gamePlayLogService;

    private User testUser;
    private VideoGame testGame;
    private Platform testPlatform;
    private UserGamePlay testPlayLog;
    private GamePlayLogResponseDto testResponseDto;
    private GamePlayLogRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@example.com");

        testGame = new VideoGame();
        testGame.setId(UUID.randomUUID());
        testGame.setTitle("The Witcher 3");

        testPlatform = new Platform();
        testPlatform.setId(UUID.randomUUID());
        testPlatform.setName("PC");

        testPlayLog = new UserGamePlay(testUser, testGame, testPlatform, PlayStatus.COMPLETED);
        testPlayLog.setId(UUID.randomUUID());

        testResponseDto = new GamePlayLogResponseDto(
                testPlayLog.getId(), testGame.getId(), testGame.getTitle(), null,
                testPlatform.getId(), testPlatform.getName(), PlayStatus.COMPLETED,
                false, 2000, LocalDate.now(), LocalDate.now(), "owned",
                LocalDateTime.now(), LocalDateTime.now()
        );

        testRequestDto = new GamePlayLogRequestDto(
                testGame.getId(), testPlatform.getId(), PlayStatus.COMPLETED,
                LocalDate.now(), LocalDate.now(), 2000, "owned", false
        );
    }

    @Nested
    @DisplayName("logPlay()")
    class LogPlay {

        @Test
        @DisplayName("should create new play log")
        void shouldCreateNewPlayLog() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(testGame.getId())).thenReturn(Optional.of(testGame));
            when(platformRepository.findById(testPlatform.getId())).thenReturn(Optional.of(testPlatform));
            when(gamePlayLogMapper.toEntity(testRequestDto)).thenReturn(testPlayLog);
            when(userGamePlayRepository.save(any(UserGamePlay.class))).thenReturn(testPlayLog);
            when(gamePlayLogMapper.toDto(testPlayLog)).thenReturn(testResponseDto);

            // When
            GamePlayLogResponseDto result = gamePlayLogService.logPlay(testUser.getEmail(), testRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testPlayLog.getId());
            verify(userGamePlayRepository).save(any(UserGamePlay.class));
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

            // When + Then
            assertThatThrownBy(() -> gamePlayLogService.logPlay(testUser.getEmail(), testRequestDto))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when game not found")
        void shouldThrowWhenGameNotFound() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(testGame.getId())).thenReturn(Optional.empty());

            // When + Then
            assertThatThrownBy(() -> gamePlayLogService.logPlay(testUser.getEmail(), testRequestDto))
                    .isInstanceOf(GameNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updatePlayLog()")
    class UpdatePlayLog {

        @Test
        @DisplayName("should update existing play log")
        void shouldUpdateExistingPlayLog() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userGamePlayRepository.findById(testPlayLog.getId())).thenReturn(Optional.of(testPlayLog));
            when(userGamePlayRepository.save(testPlayLog)).thenReturn(testPlayLog);
            when(gamePlayLogMapper.toDto(testPlayLog)).thenReturn(testResponseDto);

            // When
            GamePlayLogResponseDto result = gamePlayLogService.updatePlayLog(testUser.getEmail(), testPlayLog.getId(), testRequestDto);

            // Then
            assertThat(result).isNotNull();
            verify(gamePlayLogMapper).updateEntityFromDto(testRequestDto, testPlayLog);
            verify(userGamePlayRepository).save(testPlayLog);
        }

        @Test
        @DisplayName("should throw when log not owned by user")
        void shouldThrowWhenLogNotOwnedByUser() {
            // Given
            User otherUser = new User();
            otherUser.setId(UUID.randomUUID());
            testPlayLog.setUser(otherUser);

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userGamePlayRepository.findById(testPlayLog.getId())).thenReturn(Optional.of(testPlayLog));

            // When + Then
            assertThatThrownBy(() -> gamePlayLogService.updatePlayLog(testUser.getEmail(), testPlayLog.getId(), testRequestDto))
                    .isInstanceOf(PlayLogNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deletePlayLog()")
    class DeletePlayLog {

        @Test
        @DisplayName("should delete play log")
        void shouldDeletePlayLog() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userGamePlayRepository.findById(testPlayLog.getId())).thenReturn(Optional.of(testPlayLog));

            // When
            gamePlayLogService.deletePlayLog(testUser.getEmail(), testPlayLog.getId());

            // Then
            verify(userGamePlayRepository).delete(testPlayLog);
        }
    }

    @Nested
    @DisplayName("getUserPlayLog()")
    class GetUserPlayLog {

        @Test
        @DisplayName("should return paginated play logs")
        void shouldReturnPaginatedPlayLogs() {
            // Given
            Page<UserGamePlay> page = new PageImpl<>(List.of(testPlayLog));
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userGamePlayRepository.findByUserId(eq(testUser.getId()), any(Pageable.class))).thenReturn(page);
            when(gamePlayLogMapper.toDto(testPlayLog)).thenReturn(testResponseDto);

            // When
            Page<GamePlayLogResponseDto> result = gamePlayLogService.getUserPlayLog(testUser.getEmail(), Pageable.unpaged());

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testResponseDto);
        }
    }

    @Nested
    @DisplayName("getGamePlayHistory()")
    class GetGamePlayHistory {

        @Test
        @DisplayName("should return list of play histories")
        void shouldReturnListOfPlayHistories() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(videoGameRepository.existsById(testGame.getId())).thenReturn(true);
            when(userGamePlayRepository.findByUserIdAndVideoGameId(testUser.getId(), testGame.getId())).thenReturn(List.of(testPlayLog));
            when(gamePlayLogMapper.toDto(testPlayLog)).thenReturn(testResponseDto);

            // When
            List<GamePlayLogResponseDto> result = gamePlayLogService.getGamePlayHistory(testUser.getEmail(), testGame.getId());

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testResponseDto);
        }
    }
}
