package com.checkpoint.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.dto.collection.WishResponseDto;
import com.checkpoint.api.dto.profile.UserProfileDto;

/**
 * Service for retrieving public user profile data.
 */
public interface ProfileService {

    /**
     * Retrieves a user's public profile by username.
     * The profile header (avatar, bio, level, badges) is always visible.
     * The {@code isFollowing} and {@code isOwner} flags are computed from the viewer's identity.
     *
     * @param username    the profile owner's username (pseudo)
     * @param viewerEmail the authenticated viewer's email, or null if anonymous
     * @return the user profile DTO
     */
    UserProfileDto getUserProfile(String username, String viewerEmail);

    /**
     * Retrieves a paginated list of reviews written by the given user.
     * Throws {@link com.checkpoint.api.exceptions.ProfilePrivateException} if the profile
     * is private and the viewer is not the owner.
     *
     * @param username    the profile owner's username (pseudo)
     * @param viewerEmail the authenticated viewer's email, or null if anonymous
     * @param pageable    pagination parameters
     * @return a page of review DTOs
     */
    Page<ReviewResponseDto> getUserReviews(String username, String viewerEmail, Pageable pageable);

    /**
     * Retrieves a paginated list of wishlist items for the given user.
     * Throws {@link com.checkpoint.api.exceptions.ProfilePrivateException} if the profile
     * is private and the viewer is not the owner.
     *
     * @param username    the profile owner's username (pseudo)
     * @param viewerEmail the authenticated viewer's email, or null if anonymous
     * @param pageable    pagination parameters
     * @return a page of wish DTOs
     */
    Page<WishResponseDto> getUserWishlist(String username, String viewerEmail, Pageable pageable);
}
