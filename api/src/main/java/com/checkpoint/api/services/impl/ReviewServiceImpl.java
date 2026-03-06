package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.mapper.ReviewMapper;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.ReviewService;

/**
 * Implementation of {@link ReviewService}.
 * Manages game reviews independently from ratings.
 */
@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final VideoGameRepository videoGameRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             VideoGameRepository videoGameRepository,
                             UserRepository userRepository,
                             ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.videoGameRepository = videoGameRepository;
        this.userRepository = userRepository;
        this.reviewMapper = reviewMapper;
    }

    @Override
    public ReviewResponseDto addOrUpdateReview(String pseudo, UUID videoGameId, ReviewRequestDto request) {
        User user = userRepository.findByEmail(pseudo)
                .orElseThrow(() -> new IllegalArgumentException("User not found with pseudo/email: " + pseudo));

        VideoGame videoGame = videoGameRepository.findById(videoGameId)
                .orElseThrow(() -> new GameNotFoundException(videoGameId));

        Review review = reviewRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId)
                .orElseGet(() -> new Review(request.content(), request.haveSpoilers(), user, videoGame));
        review.setContent(request.content());
        review.setHaveSpoilers(request.haveSpoilers());
        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getGameReviews(UUID videoGameId, Pageable pageable) {
        if (!videoGameRepository.existsById(videoGameId)) {
            throw new GameNotFoundException(videoGameId);
        }

        Page<Review> reviews = reviewRepository.findByVideoGameId(videoGameId, pageable);

        return reviews.map(reviewMapper::toDto);
    }

    @Override
    public void deleteReview(String pseudo, UUID videoGameId) {
        User user = userRepository.findByEmail(pseudo)
                .orElseThrow(() -> new IllegalArgumentException("User not found with pseudo/email: " + pseudo));

        Review review = reviewRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for this user and game"));

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewByUserAndGame(String pseudo, UUID videoGameId) {
        User user = userRepository.findByEmail(pseudo)
                .orElseThrow(() -> new IllegalArgumentException("User not found with pseudo/email: " + pseudo));

        if (!videoGameRepository.existsById(videoGameId)) {
            throw new GameNotFoundException(videoGameId);
        }

        Review review = reviewRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId)
                .orElse(null);

        if (review == null) {
            return null;
        }

        return reviewMapper.toDto(review);
    }
}
