package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.dto.tag.TagRequestDto;
import com.checkpoint.api.dto.tag.TagResponseDto;
import com.checkpoint.api.entities.Platform;
import com.checkpoint.api.entities.Tag;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.exceptions.DuplicateTagException;
import com.checkpoint.api.exceptions.TagNotFoundException;
import com.checkpoint.api.exceptions.UserNotFoundException;
import com.checkpoint.api.mapper.GamePlayLogMapper;
import com.checkpoint.api.mapper.TagMapper;
import com.checkpoint.api.repositories.TagRepository;
import com.checkpoint.api.repositories.UserRepository;

/**
 * Unit tests for {@link TagServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private GamePlayLogMapper gamePlayLogMapper;

    private TagServiceImpl tagService;

    private User testUser;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository, userRepository, tagMapper, gamePlayLogMapper);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setPseudo("testuser");
        testUser.setEmail("test@test.com");

        testTag = new Tag("cozy", testUser);
        testTag.setId(UUID.randomUUID());
        testTag.setPlayLogs(new HashSet<>());
    }

    @Nested
    @DisplayName("createTag")
    class CreateTag {

        @Test
        @DisplayName("should create a tag successfully")
        void createTag_shouldCreateTag() {
            // Given
            TagRequestDto request = new TagRequestDto("Cozy");
            TagResponseDto expectedResponse = new TagResponseDto(testTag.getId(), "cozy", 0);

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.existsByUserIdAndNameIgnoreCase(testUser.getId(), "cozy")).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(testTag);
            when(tagMapper.toDto(testTag, 0)).thenReturn(expectedResponse);

            // When
            TagResponseDto result = tagService.createTag("test@test.com", request);

            // Then
            assertThat(result.name()).isEqualTo("cozy");
            assertThat(result.playLogsCount()).isZero();
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("should throw DuplicateTagException when name already exists")
        void createTag_shouldThrowWhenDuplicate() {
            // Given
            TagRequestDto request = new TagRequestDto("Cozy");

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.existsByUserIdAndNameIgnoreCase(testUser.getId(), "cozy")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> tagService.createTag("test@test.com", request))
                    .isInstanceOf(DuplicateTagException.class);
        }
    }

    @Nested
    @DisplayName("getUserTags")
    class GetUserTags {

        @Test
        @DisplayName("should return all user tags with counts")
        void getUserTags_shouldReturnTags() {
            // Given
            TagResponseDto dto = new TagResponseDto(testTag.getId(), "cozy", 0);

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByUserIdOrderByNameAsc(testUser.getId())).thenReturn(List.of(testTag));
            when(tagMapper.toDto(testTag, 0)).thenReturn(dto);

            // When
            List<TagResponseDto> result = tagService.getUserTags("test@test.com");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("cozy");
        }
    }

    @Nested
    @DisplayName("getPublicUserTags")
    class GetPublicUserTags {

        @Test
        @DisplayName("should return public user tags")
        void getPublicUserTags_shouldReturnTags() {
            // Given
            TagResponseDto dto = new TagResponseDto(testTag.getId(), "cozy", 0);

            when(userRepository.findByPseudo("testuser")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByUserPseudoOrderByNameAsc("testuser")).thenReturn(List.of(testTag));
            when(tagMapper.toDto(testTag, 0)).thenReturn(dto);

            // When
            List<TagResponseDto> result = tagService.getPublicUserTags("testuser");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void getPublicUserTags_shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findByPseudo("unknown")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tagService.getPublicUserTags("unknown"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateTag")
    class UpdateTag {

        @Test
        @DisplayName("should rename a tag successfully")
        void updateTag_shouldRenameTag() {
            // Given
            TagRequestDto request = new TagRequestDto("Relaxing");
            TagResponseDto expectedResponse = new TagResponseDto(testTag.getId(), "relaxing", 0);

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByIdAndUserId(testTag.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testTag));
            when(tagRepository.existsByUserIdAndNameIgnoreCase(testUser.getId(), "relaxing")).thenReturn(false);
            when(tagRepository.save(testTag)).thenReturn(testTag);
            when(tagMapper.toDto(testTag, 0)).thenReturn(expectedResponse);

            // When
            TagResponseDto result = tagService.updateTag("test@test.com", testTag.getId(), request);

            // Then
            assertThat(result.name()).isEqualTo("relaxing");
        }

        @Test
        @DisplayName("should throw TagNotFoundException when tag not found")
        void updateTag_shouldThrowWhenNotFound() {
            // Given
            UUID tagId = UUID.randomUUID();
            TagRequestDto request = new TagRequestDto("Relaxing");

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByIdAndUserId(tagId, testUser.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tagService.updateTag("test@test.com", tagId, request))
                    .isInstanceOf(TagNotFoundException.class);
        }

        @Test
        @DisplayName("should throw DuplicateTagException when new name already exists")
        void updateTag_shouldThrowWhenDuplicateName() {
            // Given
            TagRequestDto request = new TagRequestDto("PS5");

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByIdAndUserId(testTag.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testTag));
            when(tagRepository.existsByUserIdAndNameIgnoreCase(testUser.getId(), "ps5")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> tagService.updateTag("test@test.com", testTag.getId(), request))
                    .isInstanceOf(DuplicateTagException.class);
        }
    }

    @Nested
    @DisplayName("deleteTag")
    class DeleteTag {

        @Test
        @DisplayName("should delete a tag successfully")
        void deleteTag_shouldDeleteTag() {
            // Given
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByIdAndUserId(testTag.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testTag));

            // When
            tagService.deleteTag("test@test.com", testTag.getId());

            // Then
            verify(tagRepository).delete(testTag);
        }

        @Test
        @DisplayName("should throw TagNotFoundException when tag not found")
        void deleteTag_shouldThrowWhenNotFound() {
            // Given
            UUID tagId = UUID.randomUUID();

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByIdAndUserId(tagId, testUser.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tagService.deleteTag("test@test.com", tagId))
                    .isInstanceOf(TagNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getPlayLogsByTag")
    class GetPlayLogsByTag {

        @Test
        @DisplayName("should return paginated play logs for a tag")
        void getPlayLogsByTag_shouldReturnPlayLogs() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            UserGamePlay playLog = createTestPlayLog();
            Page<UserGamePlay> playLogPage = new PageImpl<>(List.of(playLog), pageable, 1);
            GamePlayLogResponseDto responseDto = new GamePlayLogResponseDto(
                    playLog.getId(), UUID.randomUUID(), "Zelda", null,
                    UUID.randomUUID(), "Switch", PlayStatus.COMPLETED,
                    false, 120, null, null, "owned",
                    null, null, false, null, 5, List.of()
            );

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByIdAndUserId(testTag.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testTag));
            when(tagRepository.findPlayLogsByTagId(testTag.getId(), pageable)).thenReturn(playLogPage);
            when(gamePlayLogMapper.toDto(playLog)).thenReturn(responseDto);

            // When
            Page<GamePlayLogResponseDto> result = tagService.getPlayLogsByTag(
                    "test@test.com", testTag.getId(), pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("Zelda");
        }
    }

    @Nested
    @DisplayName("getPublicPlayLogsByTag")
    class GetPublicPlayLogsByTag {

        @Test
        @DisplayName("should return paginated play logs for a public tag")
        void getPublicPlayLogsByTag_shouldReturnPlayLogs() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            UserGamePlay playLog = createTestPlayLog();
            Page<UserGamePlay> playLogPage = new PageImpl<>(List.of(playLog), pageable, 1);
            GamePlayLogResponseDto responseDto = new GamePlayLogResponseDto(
                    playLog.getId(), UUID.randomUUID(), "Zelda", null,
                    UUID.randomUUID(), "Switch", PlayStatus.COMPLETED,
                    false, 120, null, null, "owned",
                    null, null, false, null, 5, List.of()
            );

            when(userRepository.findByPseudo("testuser")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByNameAndUserPseudo("cozy", "testuser"))
                    .thenReturn(Optional.of(testTag));
            when(tagRepository.findPlayLogsByTagNameAndUserPseudo("cozy", "testuser", pageable))
                    .thenReturn(playLogPage);
            when(gamePlayLogMapper.toDto(playLog)).thenReturn(responseDto);

            // When
            Page<GamePlayLogResponseDto> result = tagService.getPublicPlayLogsByTag(
                    "testuser", "cozy", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should throw TagNotFoundException when tag not found")
        void getPublicPlayLogsByTag_shouldThrowWhenTagNotFound() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);

            when(userRepository.findByPseudo("testuser")).thenReturn(Optional.of(testUser));
            when(tagRepository.findByNameAndUserPseudo("unknown", "testuser"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tagService.getPublicPlayLogsByTag("testuser", "unknown", pageable))
                    .isInstanceOf(TagNotFoundException.class);
        }
    }

    private UserGamePlay createTestPlayLog() {
        VideoGame videoGame = new VideoGame();
        videoGame.setId(UUID.randomUUID());
        videoGame.setTitle("Zelda");

        Platform platform = new Platform();
        platform.setId(UUID.randomUUID());
        platform.setName("Switch");

        UserGamePlay playLog = new UserGamePlay(testUser, videoGame, platform, PlayStatus.COMPLETED);
        playLog.setId(UUID.randomUUID());
        playLog.setTags(new HashSet<>());
        return playLog;
    }
}
