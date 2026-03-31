package com.checkpoint.api.services.impl;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.social.LikeResponseDto;
import com.checkpoint.api.entities.GameList;
import com.checkpoint.api.entities.Like;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.GameListNotFoundException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.repositories.GameListRepository;
import com.checkpoint.api.repositories.LikeRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.LikeService;

/**
 * Implementation of {@link LikeService}.
 * Manages like/unlike toggle operations on reviews and game lists.
 */
@Service
@Transactional
public class LikeServiceImpl implements LikeService {

    private static final Logger log = LoggerFactory.getLogger(LikeServiceImpl.class);

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final GameListRepository gameListRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new LikeServiceImpl.
     *
     * @param likeRepository     the like repository
     * @param reviewRepository   the review repository
     * @param gameListRepository the game list repository
     * @param userRepository     the user repository
     */
    public LikeServiceImpl(LikeRepository likeRepository,
                           ReviewRepository reviewRepository,
                           GameListRepository gameListRepository,
                           UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.reviewRepository = reviewRepository;
        this.gameListRepository = gameListRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LikeResponseDto toggleReviewLike(String userEmail, UUID reviewId) {
        User user = getUserByEmail(userEmail);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        Optional<Like> existingLike = likeRepository.findByUserIdAndReviewId(user.getId(), reviewId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            long likesCount = likeRepository.countByReviewId(reviewId) - 1;
            log.info("User {} unliked review {}", user.getPseudo(), reviewId);
            return new LikeResponseDto(false, Math.max(0, likesCount));
        } else {
            Like like = Like.forReview(user, review);
            likeRepository.save(like);
            long likesCount = likeRepository.countByReviewId(reviewId) + 1;
            log.info("User {} liked review {}", user.getPseudo(), reviewId);
            return new LikeResponseDto(true, likesCount);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LikeResponseDto toggleListLike(String userEmail, UUID listId) {
        User user = getUserByEmail(userEmail);

        GameList gameList = gameListRepository.findById(listId)
                .orElseThrow(() -> new GameListNotFoundException(listId));

        Optional<Like> existingLike = likeRepository.findByUserIdAndGameListId(user.getId(), listId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            long likesCount = likeRepository.countByGameListId(listId) - 1;
            log.info("User {} unliked list {}", user.getPseudo(), listId);
            return new LikeResponseDto(false, Math.max(0, likesCount));
        } else {
            Like like = Like.forGameList(user, gameList);
            likeRepository.save(like);
            long likesCount = likeRepository.countByGameListId(listId) + 1;
            log.info("User {} liked list {}", user.getPseudo(), listId);
            return new LikeResponseDto(true, likesCount);
        }
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
