package com.checkpoint.api.services.impl;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.collection.GameInteractionStatusDto;
import com.checkpoint.api.entities.Rate;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.UserGame;
import com.checkpoint.api.enums.GameStatus;
import com.checkpoint.api.repositories.BacklogRepository;
import com.checkpoint.api.repositories.RateRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserGamePlayRepository;
import com.checkpoint.api.repositories.UserGameRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.WishRepository;
import com.checkpoint.api.services.GameInteractionService;

@Service
@Transactional(readOnly = true)
public class GameInteractionServiceImpl implements GameInteractionService {

    private static final Logger log = LoggerFactory.getLogger(GameInteractionServiceImpl.class);

    private final UserRepository userRepository;
    private final WishRepository wishRepository;
    private final BacklogRepository backlogRepository;
    private final UserGameRepository userGameRepository;
    private final UserGamePlayRepository userGamePlayRepository;
    private final RateRepository rateRepository;
    private final ReviewRepository reviewRepository;

    public GameInteractionServiceImpl(
            UserRepository userRepository,
            WishRepository wishRepository,
            BacklogRepository backlogRepository,
            UserGameRepository userGameRepository,
            UserGamePlayRepository userGamePlayRepository,
            RateRepository rateRepository,
            ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.wishRepository = wishRepository;
        this.backlogRepository = backlogRepository;
        this.userGameRepository = userGameRepository;
        this.userGamePlayRepository = userGamePlayRepository;
        this.rateRepository = rateRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public GameInteractionStatusDto getGameInteractionStatus(String userEmail, UUID videoGameId) {
        log.debug("Fetching game interaction status for user {} and game {}", userEmail, videoGameId);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        boolean inWishlist = wishRepository.existsByUserIdAndVideoGameId(user.getId(), videoGameId);
        boolean inBacklog = backlogRepository.existsByUserIdAndVideoGameId(user.getId(), videoGameId);

        Optional<UserGame> userGameOpt = userGameRepository.findByUserIdAndVideoGameId(user.getId(), videoGameId);
        boolean inLibrary = userGameOpt.isPresent();
        GameStatus libraryStatus = userGameOpt.map(UserGame::getStatus).orElse(null);

        int playCount = (int) userGamePlayRepository.countByUserIdAndVideoGameId(user.getId(), videoGameId);

        Optional<Rate> rateOpt = rateRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId);
        Integer userRating = rateOpt.map(Rate::getScore).orElse(null);

        boolean hasReview = reviewRepository.findByUserPseudoAndVideoGameId(user.getPseudo(), videoGameId).isPresent();

        return new GameInteractionStatusDto(
                inWishlist,
                inBacklog,
                inLibrary,
                libraryStatus,
                playCount,
                userRating,
                hasReview
        );
    }
}
