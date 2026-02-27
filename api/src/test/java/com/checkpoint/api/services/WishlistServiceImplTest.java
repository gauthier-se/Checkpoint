package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.checkpoint.api.dto.collection.WishResponseDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.entities.Wish;
import com.checkpoint.api.exceptions.GameAlreadyInWishlistException;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.GameNotInWishlistException;
import com.checkpoint.api.mapper.WishMapper;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.repositories.WishRepository;
import com.checkpoint.api.services.impl.WishlistServiceImpl;

/**
 * Unit tests for {@link WishlistServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishRepository wishRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoGameRepository videoGameRepository;

    @Mock
    private WishMapper wishMapper;

    private WishlistServiceImpl service;

    private User testUser;
    private VideoGame testGame;
    private Wish testWish;
    private WishResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        service = new WishlistServiceImpl(
                wishRepository, userRepository, videoGameRepository, wishMapper);

        testUser = new User("testuser", "user@example.com", "password");
        testUser.setId(UUID.randomUUID());

        testGame = new VideoGame("The Witcher 3", "Epic RPG", LocalDate.of(2015, 5, 19));
        testGame.setId(UUID.randomUUID());
        testGame.setCoverUrl("cover.jpg");

        testWish = new Wish(testUser, testGame);
        testWish.setId(UUID.randomUUID());
        testWish.setCreatedAt(LocalDateTime.now());
        testWish.setUpdatedAt(LocalDateTime.now());

        testResponseDto = new WishResponseDto(
                testWish.getId(), testGame.getId(), testGame.getTitle(),
                testGame.getCoverUrl(), testGame.getReleaseDate(),
                testWish.getCreatedAt());
    }

    @Nested
    @DisplayName("addToWishlist")
    class AddToWishlist {

        @Test
        @DisplayName("should add game to wishlist successfully")
        void shouldAddGameSuccessfully() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(testGame.getId())).thenReturn(Optional.of(testGame));
            when(wishRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId())).thenReturn(false);
            when(wishRepository.save(any(Wish.class))).thenReturn(testWish);
            when(wishMapper.toResponseDto(testWish)).thenReturn(testResponseDto);

            // When
            WishResponseDto result = service.addToWishlist("user@example.com", testGame.getId());

            // Then
            assertThat(result.videoGameId()).isEqualTo(testGame.getId());
            assertThat(result.title()).isEqualTo("The Witcher 3");

            ArgumentCaptor<Wish> captor = ArgumentCaptor.forClass(Wish.class);
            verify(wishRepository).save(captor.capture());
            assertThat(captor.getValue().getUser()).isEqualTo(testUser);
            assertThat(captor.getValue().getVideoGame()).isEqualTo(testGame);
        }

        @Test
        @DisplayName("should throw GameAlreadyInWishlistException when game already in wishlist")
        void shouldThrowWhenGameAlreadyInWishlist() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(testGame.getId())).thenReturn(Optional.of(testGame));
            when(wishRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId())).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> service.addToWishlist("user@example.com", testGame.getId()))
                    .isInstanceOf(GameAlreadyInWishlistException.class)
                    .hasMessageContaining(testGame.getId().toString());

            verify(wishRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw GameNotFoundException when video game does not exist")
        void shouldThrowWhenVideoGameNotFound() {
            // Given
            UUID unknownGameId = UUID.randomUUID();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(videoGameRepository.findById(unknownGameId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.addToWishlist("user@example.com", unknownGameId))
                    .isInstanceOf(GameNotFoundException.class)
                    .hasMessageContaining(unknownGameId.toString());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when user not found")
        void shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.addToWishlist("unknown@example.com", testGame.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unknown@example.com");
        }
    }

    @Nested
    @DisplayName("removeFromWishlist")
    class RemoveFromWishlist {

        @Test
        @DisplayName("should remove game from wishlist successfully")
        void shouldRemoveGameSuccessfully() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(wishRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId()))
                    .thenReturn(true);

            // When
            service.removeFromWishlist("user@example.com", testGame.getId());

            // Then
            verify(wishRepository).deleteByUserIdAndVideoGameId(testUser.getId(), testGame.getId());
        }

        @Test
        @DisplayName("should throw GameNotInWishlistException when game not in wishlist")
        void shouldThrowWhenGameNotInWishlist() {
            // Given
            UUID videoGameId = UUID.randomUUID();
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(wishRepository.existsByUserIdAndVideoGameId(testUser.getId(), videoGameId))
                    .thenReturn(false);

            // When / Then
            assertThatThrownBy(() -> service.removeFromWishlist("user@example.com", videoGameId))
                    .isInstanceOf(GameNotInWishlistException.class)
                    .hasMessageContaining(videoGameId.toString());

            verify(wishRepository, never()).deleteByUserIdAndVideoGameId(any(), any());
        }
    }

    @Nested
    @DisplayName("getUserWishlist")
    class GetUserWishlist {

        @Test
        @DisplayName("should return paginated user wishlist")
        void shouldReturnPaginatedWishlist() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Wish> wishPage = new PageImpl<>(List.of(testWish), pageable, 1);

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(wishRepository.findByUserIdWithVideoGame(testUser.getId(), pageable))
                    .thenReturn(wishPage);
            when(wishMapper.toResponseDto(testWish)).thenReturn(testResponseDto);

            // When
            Page<WishResponseDto> result = service.getUserWishlist("user@example.com", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("The Witcher 3");
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty page when wishlist is empty")
        void shouldReturnEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Wish> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(wishRepository.findByUserIdWithVideoGame(testUser.getId(), pageable))
                    .thenReturn(emptyPage);

            // When
            Page<WishResponseDto> result = service.getUserWishlist("user@example.com", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("isInWishlist")
    class IsInWishlist {

        @Test
        @DisplayName("should return true when game is in wishlist")
        void shouldReturnTrueWhenInWishlist() {
            // Given
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(wishRepository.existsByUserIdAndVideoGameId(testUser.getId(), testGame.getId()))
                    .thenReturn(true);

            // When
            boolean result = service.isInWishlist("user@example.com", testGame.getId());

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when game is not in wishlist")
        void shouldReturnFalseWhenNotInWishlist() {
            // Given
            UUID videoGameId = UUID.randomUUID();
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(wishRepository.existsByUserIdAndVideoGameId(testUser.getId(), videoGameId))
                    .thenReturn(false);

            // When
            boolean result = service.isInWishlist("user@example.com", videoGameId);

            // Then
            assertThat(result).isFalse();
        }
    }
}
