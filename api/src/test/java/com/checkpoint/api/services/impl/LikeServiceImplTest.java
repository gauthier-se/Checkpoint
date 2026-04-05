package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.dto.social.LikeResponseDto;
import com.checkpoint.api.entities.Comment;
import com.checkpoint.api.entities.GameList;
import com.checkpoint.api.entities.Like;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.CommentNotFoundException;
import com.checkpoint.api.exceptions.GameListNotFoundException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.repositories.CommentRepository;
import com.checkpoint.api.repositories.GameListRepository;
import com.checkpoint.api.repositories.LikeRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;

/**
 * Unit tests for {@link LikeServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private GameListRepository gameListRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    private LikeServiceImpl likeService;

    private User user;
    private Review review;
    private GameList gameList;

    @BeforeEach
    void setUp() {
        likeService = new LikeServiceImpl(
                likeRepository, reviewRepository, gameListRepository,
                commentRepository, userRepository);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPseudo("testUser");

        review = new Review();
        review.setId(UUID.randomUUID());

        gameList = new GameList();
        gameList.setId(UUID.randomUUID());
    }

    @Nested
    @DisplayName("toggleReviewLike")
    class ToggleReviewLike {

        @Test
        @DisplayName("should like when not already liked")
        void toggleReviewLike_shouldLikeWhenNotLiked() {
            // Given
            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));
            when(likeRepository.findByUserIdAndReviewId(user.getId(), review.getId()))
                    .thenReturn(Optional.empty());
            when(likeRepository.countByReviewId(review.getId()))
                    .thenReturn(3L);

            // When
            LikeResponseDto result = likeService.toggleReviewLike("user@example.com", review.getId());

            // Then
            assertThat(result.liked()).isTrue();
            assertThat(result.likesCount()).isEqualTo(4);
            verify(likeRepository).save(any(Like.class));
        }

        @Test
        @DisplayName("should unlike when already liked")
        void toggleReviewLike_shouldUnlikeWhenAlreadyLiked() {
            // Given
            Like existingLike = Like.forReview(user, review);

            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));
            when(likeRepository.findByUserIdAndReviewId(user.getId(), review.getId()))
                    .thenReturn(Optional.of(existingLike));
            when(likeRepository.countByReviewId(review.getId()))
                    .thenReturn(4L);

            // When
            LikeResponseDto result = likeService.toggleReviewLike("user@example.com", review.getId());

            // Then
            assertThat(result.liked()).isFalse();
            assertThat(result.likesCount()).isEqualTo(3);
            verify(likeRepository).delete(existingLike);
        }

        @Test
        @DisplayName("should throw ReviewNotFoundException when review does not exist")
        void toggleReviewLike_shouldThrowWhenReviewNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(reviewRepository.findById(unknownId))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> likeService.toggleReviewLike("user@example.com", unknownId))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("toggleListLike")
    class ToggleListLike {

        @Test
        @DisplayName("should like when not already liked")
        void toggleListLike_shouldLikeWhenNotLiked() {
            // Given
            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(gameListRepository.findById(gameList.getId()))
                    .thenReturn(Optional.of(gameList));
            when(likeRepository.findByUserIdAndGameListId(user.getId(), gameList.getId()))
                    .thenReturn(Optional.empty());
            when(likeRepository.countByGameListId(gameList.getId()))
                    .thenReturn(7L);

            // When
            LikeResponseDto result = likeService.toggleListLike("user@example.com", gameList.getId());

            // Then
            assertThat(result.liked()).isTrue();
            assertThat(result.likesCount()).isEqualTo(8);
            verify(likeRepository).save(any(Like.class));
        }

        @Test
        @DisplayName("should unlike when already liked")
        void toggleListLike_shouldUnlikeWhenAlreadyLiked() {
            // Given
            Like existingLike = Like.forGameList(user, gameList);

            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(gameListRepository.findById(gameList.getId()))
                    .thenReturn(Optional.of(gameList));
            when(likeRepository.findByUserIdAndGameListId(user.getId(), gameList.getId()))
                    .thenReturn(Optional.of(existingLike));
            when(likeRepository.countByGameListId(gameList.getId()))
                    .thenReturn(8L);

            // When
            LikeResponseDto result = likeService.toggleListLike("user@example.com", gameList.getId());

            // Then
            assertThat(result.liked()).isFalse();
            assertThat(result.likesCount()).isEqualTo(7);
            verify(likeRepository).delete(existingLike);
        }

        @Test
        @DisplayName("should throw GameListNotFoundException when list does not exist")
        void toggleListLike_shouldThrowWhenListNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(gameListRepository.findById(unknownId))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> likeService.toggleListLike("user@example.com", unknownId))
                    .isInstanceOf(GameListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("toggleCommentLike")
    class ToggleCommentLike {

        @Test
        @DisplayName("should like when not already liked")
        void toggleCommentLike_shouldLikeWhenNotLiked() {
            // Given
            Comment comment = new Comment();
            comment.setId(UUID.randomUUID());

            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(commentRepository.findById(comment.getId()))
                    .thenReturn(Optional.of(comment));
            when(likeRepository.findByUserIdAndCommentId(user.getId(), comment.getId()))
                    .thenReturn(Optional.empty());
            when(likeRepository.countByCommentId(comment.getId()))
                    .thenReturn(2L);

            // When
            LikeResponseDto result = likeService.toggleCommentLike("user@example.com", comment.getId());

            // Then
            assertThat(result.liked()).isTrue();
            assertThat(result.likesCount()).isEqualTo(3);
            verify(likeRepository).save(any(Like.class));
        }

        @Test
        @DisplayName("should unlike when already liked")
        void toggleCommentLike_shouldUnlikeWhenAlreadyLiked() {
            // Given
            Comment comment = new Comment();
            comment.setId(UUID.randomUUID());
            Like existingLike = Like.forComment(user, comment);

            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(commentRepository.findById(comment.getId()))
                    .thenReturn(Optional.of(comment));
            when(likeRepository.findByUserIdAndCommentId(user.getId(), comment.getId()))
                    .thenReturn(Optional.of(existingLike));
            when(likeRepository.countByCommentId(comment.getId()))
                    .thenReturn(3L);

            // When
            LikeResponseDto result = likeService.toggleCommentLike("user@example.com", comment.getId());

            // Then
            assertThat(result.liked()).isFalse();
            assertThat(result.likesCount()).isEqualTo(2);
            verify(likeRepository).delete(existingLike);
        }

        @Test
        @DisplayName("should throw CommentNotFoundException when comment does not exist")
        void toggleCommentLike_shouldThrowWhenCommentNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(commentRepository.findById(unknownId))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> likeService.toggleCommentLike("user@example.com", unknownId))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }
}
