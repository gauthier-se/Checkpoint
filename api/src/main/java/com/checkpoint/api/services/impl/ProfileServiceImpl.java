package com.checkpoint.api.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.dto.collection.WishResponseDto;
import com.checkpoint.api.dto.profile.UserProfileDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.ProfilePrivateException;
import com.checkpoint.api.exceptions.UserNotFoundException;
import com.checkpoint.api.mapper.ProfileMapper;
import com.checkpoint.api.mapper.ReviewMapper;
import com.checkpoint.api.mapper.WishMapper;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.WishRepository;
import com.checkpoint.api.services.ProfileService;

/**
 * Implementation of {@link ProfileService}.
 * Provides public user profile data with privacy enforcement.
 */
@Service
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final WishRepository wishRepository;
    private final ProfileMapper profileMapper;
    private final ReviewMapper reviewMapper;
    private final WishMapper wishMapper;

    /**
     * Constructs a new ProfileServiceImpl.
     *
     * @param userRepository   the user repository
     * @param reviewRepository the review repository
     * @param wishRepository   the wish repository
     * @param profileMapper    the profile mapper
     * @param reviewMapper     the review mapper
     * @param wishMapper       the wish mapper
     */
    public ProfileServiceImpl(UserRepository userRepository,
                               ReviewRepository reviewRepository,
                               WishRepository wishRepository,
                               ProfileMapper profileMapper,
                               ReviewMapper reviewMapper,
                               WishMapper wishMapper) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.wishRepository = wishRepository;
        this.profileMapper = profileMapper;
        this.reviewMapper = reviewMapper;
        this.wishMapper = wishMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfileDto getUserProfile(String username, String viewerEmail) {
        log.info("Fetching profile for user: {}", username);

        User user = userRepository.findByPseudoWithBadges(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        Long followerCount = userRepository.countFollowersByUserId(user.getId());
        Long followingCount = userRepository.countFollowingByUserId(user.getId());
        Long reviewCount = reviewRepository.countByUserPseudo(username);
        Long wishlistCount = wishRepository.countByUserPseudo(username);

        Boolean isFollowing = null;
        Boolean isOwner = false;

        if (viewerEmail != null) {
            User viewer = userRepository.findByEmail(viewerEmail).orElse(null);
            if (viewer != null) {
                isOwner = viewer.getId().equals(user.getId());
                isFollowing = userRepository.isFollowing(viewer.getId(), user.getId());
            }
        }

        return profileMapper.toUserProfileDto(
                user, followerCount, followingCount,
                reviewCount, wishlistCount, isFollowing, isOwner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ReviewResponseDto> getUserReviews(String username, String viewerEmail, Pageable pageable) {
        log.info("Fetching reviews for user: {}", username);

        User user = userRepository.findByPseudo(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        enforcePrivacy(user, viewerEmail);

        return reviewRepository.findByUserPseudo(username, pageable)
                .map(reviewMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<WishResponseDto> getUserWishlist(String username, String viewerEmail, Pageable pageable) {
        log.info("Fetching wishlist for user: {}", username);

        User user = userRepository.findByPseudo(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        enforcePrivacy(user, viewerEmail);

        return wishRepository.findByUserPseudoWithVideoGame(username, pageable)
                .map(wishMapper::toResponseDto);
    }

    /**
     * Checks if the profile is private and the viewer is not the owner.
     * Throws {@link ProfilePrivateException} if access is denied.
     *
     * @param profileOwner the profile owner
     * @param viewerEmail  the viewer's email, or null if anonymous
     */
    private void enforcePrivacy(User profileOwner, String viewerEmail) {
        if (!Boolean.TRUE.equals(profileOwner.getIsPrivate())) {
            return;
        }

        if (viewerEmail != null) {
            User viewer = userRepository.findByEmail(viewerEmail).orElse(null);
            if (viewer != null && viewer.getId().equals(profileOwner.getId())) {
                return;
            }
        }

        throw new ProfilePrivateException(profileOwner.getPseudo());
    }
}
