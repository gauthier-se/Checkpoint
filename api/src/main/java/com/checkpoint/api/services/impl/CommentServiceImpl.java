package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.social.CommentResponseDto;
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
import com.checkpoint.api.services.CommentService;

/**
 * Implementation of {@link CommentService}.
 * Manages CRUD operations for comments on reviews and game lists.
 */
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final GameListRepository gameListRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    /**
     * Constructs a new CommentServiceImpl.
     *
     * @param commentRepository  the comment repository
     * @param reviewRepository   the review repository
     * @param gameListRepository the game list repository
     * @param userRepository     the user repository
     * @param commentMapper      the comment mapper
     */
    public CommentServiceImpl(CommentRepository commentRepository,
                              ReviewRepository reviewRepository,
                              GameListRepository gameListRepository,
                              UserRepository userRepository,
                              CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
        this.gameListRepository = gameListRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponseDto addReviewComment(String userEmail, UUID reviewId, String content) {
        User user = getUserByEmail(userEmail);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        Comment comment = Comment.onReview(content, user, review);
        Comment savedComment = commentRepository.save(comment);

        log.info("User {} commented on review {}", user.getPseudo(), reviewId);

        return commentMapper.toDto(savedComment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponseDto addListComment(String userEmail, UUID listId, String content) {
        User user = getUserByEmail(userEmail);

        GameList gameList = gameListRepository.findById(listId)
                .orElseThrow(() -> new GameListNotFoundException(listId));

        Comment comment = Comment.onList(content, user, gameList);
        Comment savedComment = commentRepository.save(comment);

        log.info("User {} commented on list {}", user.getPseudo(), listId);

        return commentMapper.toDto(savedComment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getReviewComments(UUID reviewId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByReviewId(reviewId, pageable);
        return comments.map(commentMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getListComments(UUID listId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByGameListId(listId, pageable);
        return comments.map(commentMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponseDto updateComment(String userEmail, UUID commentId, String content) {
        User user = getUserByEmail(userEmail);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedCommentAccessException(commentId);
        }

        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);

        log.info("User {} updated comment {}", user.getPseudo(), commentId);

        return commentMapper.toDto(updatedComment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteComment(String userEmail, UUID commentId) {
        User user = getUserByEmail(userEmail);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedCommentAccessException(commentId);
        }

        commentRepository.delete(comment);

        log.info("User {} deleted comment {}", user.getPseudo(), commentId);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email the user's email
     * @return the user entity
     * @throws IllegalArgumentException if the user is not found
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }
}
