package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.PlayLogNotFoundException;
import com.checkpoint.api.exceptions.ReviewAlreadyExistsException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.mapper.ReviewMapper;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserGamePlayRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.ReviewService;

/**
 * Implementation of {@link ReviewService}.
 * Manages game reviews tied to play log entries.
 */
@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final VideoGameRepository videoGameRepository;
    private final UserRepository userRepository;
    private final UserGamePlayRepository userGamePlayRepository;
    private final ReviewMapper reviewMapper;

    /**
     * Constructs a new ReviewServiceImpl.
     *
     * @param reviewRepository       the review repository
     * @param videoGameRepository    the video game repository
     * @param userRepository         the user repository
     * @param userGamePlayRepository the play log repository
     * @param reviewMapper           the review mapper
     */
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             VideoGameRepository videoGameRepository,
                             UserRepository userRepository,
                             UserGamePlayRepository userGamePlayRepository,
                             ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.videoGameRepository = videoGameRepository;
        this.userRepository = userRepository;
        this.userGamePlayRepository = userGamePlayRepository;
        this.reviewMapper = reviewMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getGameReviews(UUID videoGameId, Pageable pageable) {
        if (!videoGameRepository.existsById(videoGameId)) {
            throw new GameNotFoundException(videoGameId);
        }

        Page<Review> reviews = reviewRepository.findByVideoGameId(videoGameId, pageable);

        return reviews.map(reviewMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReviewResponseDto createPlayLogReview(String userEmail, UUID playId, ReviewRequestDto request) {
        User user = getUserByEmail(userEmail);
        UserGamePlay playLog = getPlayLogOwnedByUser(playId, user);

        if (reviewRepository.existsByUserGamePlayId(playId)) {
            throw new ReviewAlreadyExistsException(playId);
        }

        Review review = new Review(
                request.content(),
                request.haveSpoilers(),
                user,
                playLog.getVideoGame(),
                playLog
        );

        Review savedReview = reviewRepository.save(review);
        log.info("Created review for play log {} by user {}", playId, user.getPseudo());

        return reviewMapper.toDto(savedReview);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReviewResponseDto updatePlayLogReview(String userEmail, UUID playId, ReviewRequestDto request) {
        User user = getUserByEmail(userEmail);
        getPlayLogOwnedByUser(playId, user);

        Review review = reviewRepository.findByUserGamePlayId(playId)
                .orElseThrow(() -> new ReviewNotFoundException(playId));

        review.setContent(request.content());
        review.setHaveSpoilers(request.haveSpoilers());
        Review savedReview = reviewRepository.save(review);
        log.info("Updated review for play log {} by user {}", playId, user.getPseudo());

        return reviewMapper.toDto(savedReview);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePlayLogReview(String userEmail, UUID playId) {
        User user = getUserByEmail(userEmail);
        getPlayLogOwnedByUser(playId, user);

        Review review = reviewRepository.findByUserGamePlayId(playId)
                .orElseThrow(() -> new ReviewNotFoundException(playId));

        reviewRepository.delete(review);
        log.info("Deleted review for play log {} by user {}", playId, user.getPseudo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDto getPlayLogReview(String userEmail, UUID playId) {
        User user = getUserByEmail(userEmail);
        getPlayLogOwnedByUser(playId, user);

        Review review = reviewRepository.findByUserGamePlayId(playId)
                .orElseThrow(() -> new ReviewNotFoundException(playId));

        return reviewMapper.toDto(review);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email the user's email
     * @return the user entity
     * @throws UsernameNotFoundException if the user is not found
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Retrieves a play log and verifies it belongs to the given user.
     *
     * @param playId the play log ID
     * @param user   the user who should own the play log
     * @return the play log entity
     * @throws PlayLogNotFoundException if the play log does not exist or does not belong to the user
     */
    private UserGamePlay getPlayLogOwnedByUser(UUID playId, User user) {
        UserGamePlay playLog = userGamePlayRepository.findById(playId)
                .orElseThrow(() -> new PlayLogNotFoundException("Play log not found with ID: " + playId));

        if (!playLog.getUser().getId().equals(user.getId())) {
            throw new PlayLogNotFoundException("Play log not found with ID: " + playId);
        }

        return playLog;
    }
}
