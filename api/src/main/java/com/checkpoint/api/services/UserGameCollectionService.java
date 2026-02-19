package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.collection.UserGameRequestDto;
import com.checkpoint.api.dto.collection.UserGameResponseDto;

/**
 * Service for managing a user's game collection (library).
 */
public interface UserGameCollectionService {

    /**
     * Adds a game to the authenticated user's library with the given status.
     *
     * @param userEmail the authenticated user's email
     * @param request   the request containing video game ID and status
     * @return the created user-game entry
     */
    UserGameResponseDto addGameToLibrary(String userEmail, UserGameRequestDto request);

    /**
     * Updates the status of a game already in the user's library.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID
     * @param request     the request containing the new status
     * @return the updated user-game entry
     */
    UserGameResponseDto updateGameStatus(String userEmail, UUID videoGameId, UserGameRequestDto request);

    /**
     * Returns the authenticated user's game collection (paginated).
     *
     * @param userEmail the authenticated user's email
     * @param pageable  pagination parameters
     * @return paginated list of games in the user's library
     */
    Page<UserGameResponseDto> getUserLibrary(String userEmail, Pageable pageable);

    /**
     * Removes a game from the user's library.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID to remove
     */
    void removeGameFromLibrary(String userEmail, UUID videoGameId);
}
