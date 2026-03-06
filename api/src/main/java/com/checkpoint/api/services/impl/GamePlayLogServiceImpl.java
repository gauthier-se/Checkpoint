package com.checkpoint.api.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.playlog.GamePlayLogRequestDto;
import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.entities.Platform;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.PlayLogNotFoundException;
import com.checkpoint.api.mapper.GamePlayLogMapper;
import com.checkpoint.api.repositories.PlatformRepository;
import com.checkpoint.api.repositories.UserGamePlayRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.GamePlayLogService;
import com.checkpoint.api.services.RateService;

/**
 * Implementation of {@link GamePlayLogService}.
 * Manages user play logs and synchronizes per-log scores with the global rating.
 */
@Service
@Transactional
public class GamePlayLogServiceImpl implements GamePlayLogService {

    private static final Logger log = LoggerFactory.getLogger(GamePlayLogServiceImpl.class);

    private final UserGamePlayRepository userGamePlayRepository;
    private final UserRepository userRepository;
    private final VideoGameRepository videoGameRepository;
    private final PlatformRepository platformRepository;
    private final GamePlayLogMapper gamePlayLogMapper;
    private final RateService rateService;

    public GamePlayLogServiceImpl(
            UserGamePlayRepository userGamePlayRepository,
            UserRepository userRepository,
            VideoGameRepository videoGameRepository,
            PlatformRepository platformRepository,
            GamePlayLogMapper gamePlayLogMapper,
            RateService rateService
    ) {
        this.userGamePlayRepository = userGamePlayRepository;
        this.userRepository = userRepository;
        this.videoGameRepository = videoGameRepository;
        this.platformRepository = platformRepository;
        this.gamePlayLogMapper = gamePlayLogMapper;
        this.rateService = rateService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>When a score is provided, the global {@link com.checkpoint.api.entities.Rate Rate}
     * is upserted so that it always reflects the most recent play log rating.</p>
     */
    @Override
    public GamePlayLogResponseDto logPlay(String userEmail, GamePlayLogRequestDto request) {
        User user = getUserByEmail(userEmail);

        VideoGame videoGame = videoGameRepository.findById(request.videoGameId())
                .orElseThrow(() -> new GameNotFoundException(request.videoGameId()));

        Platform platform = platformRepository.findById(request.platformId())
                .orElseThrow(() -> new IllegalArgumentException("Platform not found with ID: " + request.platformId()));

        UserGamePlay playLog = gamePlayLogMapper.toEntity(request);
        playLog.setUser(user);
        playLog.setVideoGame(videoGame);
        playLog.setPlatform(platform);

        UserGamePlay savedPlayLog = userGamePlayRepository.save(playLog);

        if (request.score() != null) {
            log.info("Syncing global rating for game {} with score {} from new play log", request.videoGameId(), request.score());
            rateService.rateGame(userEmail, request.videoGameId(), request.score());
        }

        return gamePlayLogMapper.toDto(savedPlayLog);
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the updated entry is the most recent scored play log for this game
     * and the score changed, the global rating is updated accordingly.
     * If the score is cleared from the most recent log, the global rating
     * falls back to the next most recent scored log or is removed entirely.</p>
     */
    @Override
    public GamePlayLogResponseDto updatePlayLog(String userEmail, UUID playId, GamePlayLogRequestDto request) {
        User user = getUserByEmail(userEmail);

        UserGamePlay playLog = userGamePlayRepository.findById(playId)
                .orElseThrow(() -> new PlayLogNotFoundException("Play log not found with ID: " + playId));

        if (!playLog.getUser().getId().equals(user.getId())) {
            throw new PlayLogNotFoundException("Play log not found with ID: " + playId);
        }

        if (request.platformId() != null && !request.platformId().equals(playLog.getPlatform().getId())) {
            Platform platform = platformRepository.findById(request.platformId())
                    .orElseThrow(() -> new IllegalArgumentException("Platform not found with ID: " + request.platformId()));
            playLog.setPlatform(platform);
        }

        if (request.videoGameId() != null && !request.videoGameId().equals(playLog.getVideoGame().getId())) {
             VideoGame videoGame = videoGameRepository.findById(request.videoGameId())
                .orElseThrow(() -> new GameNotFoundException(request.videoGameId()));
             playLog.setVideoGame(videoGame);
        }

        Integer previousScore = playLog.getScore();
        gamePlayLogMapper.updateEntityFromDto(request, playLog);

        UserGamePlay updatedPlayLog = userGamePlayRepository.save(playLog);

        syncGlobalRatingAfterUpdate(userEmail, user.getId(), playLog.getVideoGame().getId(),
                updatedPlayLog, previousScore, request.score());

        return gamePlayLogMapper.toDto(updatedPlayLog);
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the deleted entry had a score, the global rating is recalculated
     * from the next most recent scored log. If no scored logs remain,
     * the global rating is removed.</p>
     */
    @Override
    public void deletePlayLog(String userEmail, UUID playId) {
        User user = getUserByEmail(userEmail);

        UserGamePlay playLog = userGamePlayRepository.findById(playId)
                .orElseThrow(() -> new PlayLogNotFoundException("Play log not found with ID: " + playId));

        if (!playLog.getUser().getId().equals(user.getId())) {
            throw new PlayLogNotFoundException("Play log not found with ID: " + playId);
        }

        Integer deletedScore = playLog.getScore();
        UUID videoGameId = playLog.getVideoGame().getId();

        userGamePlayRepository.delete(playLog);

        if (deletedScore != null) {
            syncGlobalRatingAfterRemoval(userEmail, user.getId(), videoGameId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<GamePlayLogResponseDto> getUserPlayLog(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);

        Page<UserGamePlay> playLogs = userGamePlayRepository.findByUserId(user.getId(), pageable);
        return playLogs.map(gamePlayLogMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<GamePlayLogResponseDto> getGamePlayHistory(String userEmail, UUID videoGameId) {
        User user = getUserByEmail(userEmail);

        if (!videoGameRepository.existsById(videoGameId)) {
            throw new GameNotFoundException(videoGameId);
        }

        List<UserGamePlay> history = userGamePlayRepository.findByUserIdAndVideoGameId(user.getId(), videoGameId);
        return history.stream()
                .map(gamePlayLogMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Synchronizes the global rating after a play log update.
     * Only acts when the updated entry is the most recent scored log and the score changed.
     *
     * @param userEmail     the authenticated user's email
     * @param userId        the user's ID
     * @param videoGameId   the video game ID
     * @param updatedPlay   the updated play log entity
     * @param previousScore the score before the update (may be null)
     * @param newScore      the score after the update (may be null)
     */
    private void syncGlobalRatingAfterUpdate(String userEmail, UUID userId, UUID videoGameId,
                                              UserGamePlay updatedPlay, Integer previousScore, Integer newScore) {
        boolean scoreChanged = (previousScore == null && newScore != null)
                || (previousScore != null && !previousScore.equals(newScore));

        if (!scoreChanged) {
            return;
        }

        if (previousScore != null && newScore == null) {
            // Score was cleared — recalculate from remaining scored logs
            syncGlobalRatingAfterRemoval(userEmail, userId, videoGameId);
            return;
        }

        Optional<UserGamePlay> mostRecentScored = userGamePlayRepository
                .findMostRecentScoredPlay(userId, videoGameId);

        if (mostRecentScored.isPresent() && mostRecentScored.get().getId().equals(updatedPlay.getId())) {
            // The updated entry is the most recent scored log — update global rating
            log.info("Updating global rating for game {} to score {} from updated play log", videoGameId, newScore);
            rateService.rateGame(userEmail, videoGameId, newScore);
        }
    }

    /**
     * Recalculates the global rating from the most recent remaining scored play log.
     * If no scored logs remain, the global rating is removed.
     *
     * @param userEmail   the authenticated user's email
     * @param userId      the user's ID
     * @param videoGameId the video game ID
     */
    private void syncGlobalRatingAfterRemoval(String userEmail, UUID userId, UUID videoGameId) {
        Optional<UserGamePlay> mostRecentScored = userGamePlayRepository
                .findMostRecentScoredPlay(userId, videoGameId);

        if (mostRecentScored.isPresent()) {
            Integer fallbackScore = mostRecentScored.get().getScore();
            log.info("Recalculating global rating for game {} to score {} from next most recent play log",
                    videoGameId, fallbackScore);
            rateService.rateGame(userEmail, videoGameId, fallbackScore);
        } else {
            log.info("Removing global rating for game {} as no scored play logs remain", videoGameId);
            rateService.removeRating(userEmail, videoGameId);
        }
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
}
