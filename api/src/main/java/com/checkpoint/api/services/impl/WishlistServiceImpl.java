package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.collection.WishResponseDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.entities.Wish;
import com.checkpoint.api.exceptions.GameAlreadyInWishlistException;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.GameNotInWishlistException;
import com.checkpoint.api.mapper.WishMapper;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.checkpoint.api.repositories.WishRepository;
import com.checkpoint.api.services.WishlistService;

/**
 * Implementation of {@link WishlistService}.
 * Manages the user's personal wishlist.
 */
@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishRepository wishRepository;
    private final UserRepository userRepository;
    private final VideoGameRepository videoGameRepository;
    private final WishMapper wishMapper;

    public WishlistServiceImpl(WishRepository wishRepository,
                               UserRepository userRepository,
                               VideoGameRepository videoGameRepository,
                               WishMapper wishMapper) {
        this.wishRepository = wishRepository;
        this.userRepository = userRepository;
        this.videoGameRepository = videoGameRepository;
        this.wishMapper = wishMapper;
    }

    @Override
    public WishResponseDto addToWishlist(String userEmail, UUID videoGameId) {
        log.debug("Adding game {} to wishlist for user {}", videoGameId, userEmail);

        User user = findUserByEmail(userEmail);
        VideoGame videoGame = findVideoGameById(videoGameId);

        if (wishRepository.existsByUserIdAndVideoGameId(user.getId(), videoGame.getId())) {
            throw new GameAlreadyInWishlistException(videoGame.getId());
        }

        Wish wish = new Wish(user, videoGame);
        Wish saved = wishRepository.save(wish);

        log.info("Game {} added to wishlist for user {}", videoGame.getTitle(), userEmail);
        return wishMapper.toResponseDto(saved);
    }

    @Override
    public void removeFromWishlist(String userEmail, UUID videoGameId) {
        log.debug("Removing game {} from wishlist for user {}", videoGameId, userEmail);

        User user = findUserByEmail(userEmail);

        if (!wishRepository.existsByUserIdAndVideoGameId(user.getId(), videoGameId)) {
            throw new GameNotInWishlistException(videoGameId);
        }

        wishRepository.deleteByUserIdAndVideoGameId(user.getId(), videoGameId);
        log.info("Game {} removed from wishlist for user {}", videoGameId, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WishResponseDto> getUserWishlist(String userEmail, Pageable pageable) {
        log.debug("Fetching wishlist for user {} - page: {}, size: {}",
                userEmail, pageable.getPageNumber(), pageable.getPageSize());

        User user = findUserByEmail(userEmail);

        return wishRepository.findByUserIdWithVideoGame(user.getId(), pageable)
                .map(wishMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(String userEmail, UUID videoGameId) {
        log.debug("Checking if game {} is in wishlist for user {}", videoGameId, userEmail);

        User user = findUserByEmail(userEmail);
        return wishRepository.existsByUserIdAndVideoGameId(user.getId(), videoGameId);
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
