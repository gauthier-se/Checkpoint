package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.collection.BacklogResponseDto;
import com.checkpoint.api.entities.Backlog;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.enums.Priority;
import com.checkpoint.api.exceptions.GameAlreadyInBacklogException;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.GameNotInBacklogException;
import com.checkpoint.api.mapper.BacklogMapper;
import com.checkpoint.api.repositories.BacklogRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.services.BacklogService;

/**
 * Implementation of {@link BacklogService}.
 * Manages the user's personal backlog.
 */
@Service
@Transactional
public class BacklogServiceImpl implements BacklogService {

    private static final Logger log = LoggerFactory.getLogger(BacklogServiceImpl.class);

    private final BacklogRepository backlogRepository;
    private final UserRepository userRepository;
    private final VideoGameRepository videoGameRepository;
    private final BacklogMapper backlogMapper;

    public BacklogServiceImpl(BacklogRepository backlogRepository,
                              UserRepository userRepository,
                              VideoGameRepository videoGameRepository,
                              BacklogMapper backlogMapper) {
        this.backlogRepository = backlogRepository;
        this.userRepository = userRepository;
        this.videoGameRepository = videoGameRepository;
        this.backlogMapper = backlogMapper;
    }

    @Override
    public BacklogResponseDto addToBacklog(String userEmail, UUID videoGameId, Priority priority) {
        log.debug("Adding game {} to backlog for user {} with priority {}",
                videoGameId, userEmail, priority);

        User user = findUserByEmail(userEmail);
        VideoGame videoGame = findVideoGameById(videoGameId);

        if (backlogRepository.existsByUserIdAndVideoGameId(user.getId(), videoGame.getId())) {
            throw new GameAlreadyInBacklogException(videoGame.getId());
        }

        Backlog backlog = new Backlog(user, videoGame);
        backlog.setPriority(priority);
        Backlog saved = backlogRepository.save(backlog);

        log.info("Game {} added to backlog for user {}", videoGame.getTitle(), userEmail);
        return backlogMapper.toResponseDto(saved);
    }

    @Override
    public void removeFromBacklog(String userEmail, UUID videoGameId) {
        log.debug("Removing game {} from backlog for user {}", videoGameId, userEmail);

        User user = findUserByEmail(userEmail);

        if (!backlogRepository.existsByUserIdAndVideoGameId(user.getId(), videoGameId)) {
            throw new GameNotInBacklogException(videoGameId);
        }

        backlogRepository.deleteByUserIdAndVideoGameId(user.getId(), videoGameId);
        log.info("Game {} removed from backlog for user {}", videoGameId, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BacklogResponseDto> getUserBacklog(String userEmail, Pageable pageable) {
        log.debug("Fetching backlog for user {} - page: {}, size: {}",
                userEmail, pageable.getPageNumber(), pageable.getPageSize());

        User user = findUserByEmail(userEmail);

        Sort.Order priorityOrder = pageable.getSort().getOrderFor("priority");
        if (priorityOrder != null) {
            Pageable unsortedPage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            Page<Backlog> page = priorityOrder.isAscending()
                    ? backlogRepository.findByUserIdWithVideoGameOrderByPriorityAsc(user.getId(), unsortedPage)
                    : backlogRepository.findByUserIdWithVideoGameOrderByPriorityDesc(user.getId(), unsortedPage);
            return page.map(backlogMapper::toResponseDto);
        }

        return backlogRepository.findByUserIdWithVideoGame(user.getId(), pageable)
                .map(backlogMapper::toResponseDto);
    }

    @Override
    public BacklogResponseDto updatePriority(String userEmail, UUID videoGameId, Priority priority) {
        log.debug("Updating priority of game {} in backlog for user {} to {}",
                videoGameId, userEmail, priority);

        User user = findUserByEmail(userEmail);
        Backlog backlog = backlogRepository.findByUserIdAndVideoGameId(user.getId(), videoGameId)
                .orElseThrow(() -> new GameNotInBacklogException(videoGameId));

        backlog.setPriority(priority);
        Backlog saved = backlogRepository.save(backlog);

        log.info("Priority of game {} in backlog for user {} set to {}",
                videoGameId, userEmail, priority);
        return backlogMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInBacklog(String userEmail, UUID videoGameId) {
        log.debug("Checking if game {} is in backlog for user {}", videoGameId, userEmail);

        User user = findUserByEmail(userEmail);
        return backlogRepository.existsByUserIdAndVideoGameId(user.getId(), videoGameId);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    private VideoGame findVideoGameById(UUID id) {
        return videoGameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
    }
}
