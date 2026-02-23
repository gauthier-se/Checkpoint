package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.entities.Rate;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.mapper.ReviewMapper;
import com.checkpoint.api.repositories.RateRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.ReviewService;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RateRepository rateRepository;
    private final VideoGameRepository videoGameRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             RateRepository rateRepository,
                             VideoGameRepository videoGameRepository,
                             UserRepository userRepository,
                             ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.rateRepository = rateRepository;
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

        // Handle Rate
        Rate rate = rateRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId)
                .orElseGet(() -> new Rate(user, videoGame, request.score()));
        rate.setScore(request.score());
        rateRepository.save(rate);

        // Handle Review
        Review review = reviewRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId)
                .orElseGet(() -> new Review(request.content(), request.haveSpoilers(), user, videoGame));
        review.setContent(request.content());
        review.setHaveSpoilers(request.haveSpoilers());
        Review savedReview = reviewRepository.save(review);

        updateGameAverageRating(videoGame);

        return reviewMapper.toDto(savedReview, rate.getScore());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getGameReviews(UUID videoGameId, Pageable pageable) {
        if (!videoGameRepository.existsById(videoGameId)) {
            throw new GameNotFoundException(videoGameId);
        }

        Page<Review> reviews = reviewRepository.findByVideoGameId(videoGameId, pageable);

        // Fetch all rates for the users in this page to populate the score
        java.util.List<UUID> userIds = reviews.stream()
                .map(r -> r.getUser().getId())
                .toList();

        java.util.Map<UUID, Integer> userScoreMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            java.util.List<Rate> rates = rateRepository.findByVideoGameIdAndUserIdIn(videoGameId, userIds);
            for (Rate r : rates) {
                userScoreMap.put(r.getUser().getId(), r.getScore());
            }
        }

        return reviews.map(review -> reviewMapper.toDto(review, userScoreMap.get(review.getUser().getId())));
    }

    @Override
    public void deleteReview(String pseudo, UUID videoGameId) {
        User user = userRepository.findByEmail(pseudo)
                .orElseThrow(() -> new IllegalArgumentException("User not found with pseudo/email: " + pseudo));

        Review review = reviewRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for this user and game"));

        Rate rate = rateRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId)
                .orElse(null);

        reviewRepository.delete(review);
        if (rate != null) {
            rateRepository.delete(rate);
        }

        VideoGame videoGame = review.getVideoGame();
        updateGameAverageRating(videoGame);
    }

    private void updateGameAverageRating(VideoGame videoGame) {
        // Needs a flush so the current uncommitted changes or deletions are visible to the average query
        rateRepository.flush();
        Double avg = rateRepository.calculateAverageRating(videoGame.getId());
        double averageRating = (avg != null) ? avg : 0.0;

        // Round to 1 decimal place if you want, or keep it as is.
        averageRating = Math.round(averageRating * 10.0) / 10.0;

        videoGame.setAverageRating(averageRating);
        videoGameRepository.save(videoGame);
    }
}
