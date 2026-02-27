package com.checkpoint.api.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

@Service
@Transactional
public class GamePlayLogServiceImpl implements GamePlayLogService {

    private final UserGamePlayRepository userGamePlayRepository;
    private final UserRepository userRepository;
    private final VideoGameRepository videoGameRepository;
    private final PlatformRepository platformRepository;
    private final GamePlayLogMapper gamePlayLogMapper;

    public GamePlayLogServiceImpl(
            UserGamePlayRepository userGamePlayRepository,
            UserRepository userRepository,
            VideoGameRepository videoGameRepository,
            PlatformRepository platformRepository,
            GamePlayLogMapper gamePlayLogMapper
    ) {
        this.userGamePlayRepository = userGamePlayRepository;
        this.userRepository = userRepository;
        this.videoGameRepository = videoGameRepository;
        this.platformRepository = platformRepository;
        this.gamePlayLogMapper = gamePlayLogMapper;
    }

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

        return gamePlayLogMapper.toDto(savedPlayLog);
    }

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

        gamePlayLogMapper.updateEntityFromDto(request, playLog);

        UserGamePlay updatedPlayLog = userGamePlayRepository.save(playLog);

        return gamePlayLogMapper.toDto(updatedPlayLog);
    }

    @Override
    public void deletePlayLog(String userEmail, UUID playId) {
        User user = getUserByEmail(userEmail);

        UserGamePlay playLog = userGamePlayRepository.findById(playId)
                .orElseThrow(() -> new PlayLogNotFoundException("Play log not found with ID: " + playId));

        if (!playLog.getUser().getId().equals(user.getId())) {
            throw new PlayLogNotFoundException("Play log not found with ID: " + playId);
        }

        userGamePlayRepository.delete(playLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GamePlayLogResponseDto> getUserPlayLog(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);

        Page<UserGamePlay> playLogs = userGamePlayRepository.findByUserId(user.getId(), pageable);
        return playLogs.map(gamePlayLogMapper::toDto);
    }

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

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
