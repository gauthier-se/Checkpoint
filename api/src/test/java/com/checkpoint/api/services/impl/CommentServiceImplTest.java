package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.social.CommentResponseDto;
import com.checkpoint.api.dto.social.CommentUserDto;
import com.checkpoint.api.entities.Comment;
import com.checkpoint.api.entities.GameList;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.CommentNotFoundException;
import com.checkpoint.api.exceptions.GameListNotFoundException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.exceptions.UnauthorizedCommentAccessException;
import com.checkpoint.api.mapper.CommentMapper;
import com.checkpoint.api.repositories.CommentRepository;
import com.checkpoint.api.repositories.GameListRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;

/**
 * Unit tests for {@link CommentServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private GameListRepository gameListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    private CommentServiceImpl commentService;

    private User testUser;
    private User otherUser;
    private Review testReview;
    private GameList testList;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(
                commentRepository, reviewRepository, gameListRepository,
                userRepository, commentMapper);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setPseudo("testuser");
        testUser.setEmail("test@test.com");

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setPseudo("otheruser");
        otherUser.setEmail("other@test.com");

        testReview = new Review();
        testReview.setId(UUID.randomUUID());

        testList = new GameList("Test List", testUser);
        testList.setId(UUID.randomUUID());
    }

    @Nested
    @DisplayName("addReviewComment()")
    class AddReviewComment {

        @Test
        @DisplayName("should create a comment on a review")
        void addReviewComment_shouldCreateComment() {
            // Given
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));

            Comment savedComment = Comment.onReview("Nice!", testUser, testReview);
            savedComment.setId(UUID.randomUUID());
            when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

            CommentResponseDto expectedDto = new CommentResponseDto(
                    savedComment.getId(), "Nice!",
                    new CommentUserDto(testUser.getId(), testUser.getPseudo(), null),
                    null, null);
            when(commentMapper.toDto(savedComment)).thenReturn(expectedDto);

            // When
            CommentResponseDto result = commentService.addReviewComment("test@test.com", testReview.getId(), "Nice!");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Nice!");
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("should throw ReviewNotFoundException when review does not exist")
        void addReviewComment_shouldThrowWhenReviewNotFound() {
            // Given
            UUID reviewId = UUID.randomUUID();
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> commentService.addReviewComment("test@test.com", reviewId, "Nice!"))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("addListComment()")
    class AddListComment {

        @Test
        @DisplayName("should create a comment on a list")
        void addListComment_shouldCreateComment() {
            // Given
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(gameListRepository.findById(testList.getId())).thenReturn(Optional.of(testList));

            Comment savedComment = Comment.onList("Cool list!", testUser, testList);
            savedComment.setId(UUID.randomUUID());
            when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

            CommentResponseDto expectedDto = new CommentResponseDto(
                    savedComment.getId(), "Cool list!",
                    new CommentUserDto(testUser.getId(), testUser.getPseudo(), null),
                    null, null);
            when(commentMapper.toDto(savedComment)).thenReturn(expectedDto);

            // When
            CommentResponseDto result = commentService.addListComment("test@test.com", testList.getId(), "Cool list!");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Cool list!");
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("should throw GameListNotFoundException when list does not exist")
        void addListComment_shouldThrowWhenListNotFound() {
            // Given
            UUID listId = UUID.randomUUID();
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(gameListRepository.findById(listId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> commentService.addListComment("test@test.com", listId, "Nice!"))
                    .isInstanceOf(GameListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getReviewComments()")
    class GetReviewComments {

        @Test
        @DisplayName("should return paginated comments for a review")
        void getReviewComments_shouldReturnPaginatedComments() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Comment comment = Comment.onReview("Great!", testUser, testReview);
            comment.setId(UUID.randomUUID());
            Page<Comment> commentPage = new PageImpl<>(List.of(comment));

            when(commentRepository.findByReviewId(testReview.getId(), pageable)).thenReturn(commentPage);

            CommentResponseDto dto = new CommentResponseDto(
                    comment.getId(), "Great!",
                    new CommentUserDto(testUser.getId(), testUser.getPseudo(), null),
                    null, null);
            when(commentMapper.toDto(comment)).thenReturn(dto);

            // When
            Page<CommentResponseDto> result = commentService.getReviewComments(testReview.getId(), pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).content()).isEqualTo("Great!");
        }
    }

    @Nested
    @DisplayName("updateComment()")
    class UpdateComment {

        @Test
        @DisplayName("should update comment when user is owner")
        void updateComment_shouldUpdateWhenOwner() {
            // Given
            Comment comment = Comment.onReview("Old content", testUser, testReview);
            comment.setId(UUID.randomUUID());

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
            when(commentRepository.save(comment)).thenReturn(comment);

            CommentResponseDto expectedDto = new CommentResponseDto(
                    comment.getId(), "New content",
                    new CommentUserDto(testUser.getId(), testUser.getPseudo(), null),
                    null, null);
            when(commentMapper.toDto(comment)).thenReturn(expectedDto);

            // When
            CommentResponseDto result = commentService.updateComment("test@test.com", comment.getId(), "New content");

            // Then
            assertThat(result.content()).isEqualTo("New content");
            verify(commentRepository).save(comment);
        }

        @Test
        @DisplayName("should throw UnauthorizedCommentAccessException when user is not owner")
        void updateComment_shouldThrowWhenNotOwner() {
            // Given
            Comment comment = Comment.onReview("Content", otherUser, testReview);
            comment.setId(UUID.randomUUID());

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

            // When / Then
            assertThatThrownBy(() -> commentService.updateComment("test@test.com", comment.getId(), "Hacked"))
                    .isInstanceOf(UnauthorizedCommentAccessException.class);
        }

        @Test
        @DisplayName("should throw CommentNotFoundException when comment does not exist")
        void updateComment_shouldThrowWhenNotFound() {
            // Given
            UUID commentId = UUID.randomUUID();
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> commentService.updateComment("test@test.com", commentId, "Updated"))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteComment()")
    class DeleteComment {

        @Test
        @DisplayName("should delete comment when user is owner")
        void deleteComment_shouldDeleteWhenOwner() {
            // Given
            Comment comment = Comment.onReview("Content", testUser, testReview);
            comment.setId(UUID.randomUUID());

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

            // When
            commentService.deleteComment("test@test.com", comment.getId());

            // Then
            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("should throw UnauthorizedCommentAccessException when user is not owner")
        void deleteComment_shouldThrowWhenNotOwner() {
            // Given
            Comment comment = Comment.onReview("Content", otherUser, testReview);
            comment.setId(UUID.randomUUID());

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

            // When / Then
            assertThatThrownBy(() -> commentService.deleteComment("test@test.com", comment.getId()))
                    .isInstanceOf(UnauthorizedCommentAccessException.class);
        }
    }
}
