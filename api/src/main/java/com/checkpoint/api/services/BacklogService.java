package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.collection.BacklogResponseDto;
import com.checkpoint.api.enums.Priority;

/**
 * Service for managing a user's backlog.
 */
public interface BacklogService {

    /**
     * Adds a game to the authenticated user's backlog.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID to add
     * @param priority    initial priority, or {@code null} for none
     * @return the created backlog entry
     */
    BacklogResponseDto addToBacklog(String userEmail, UUID videoGameId, Priority priority);

    /**
     * Removes a game from the authenticated user's backlog.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID to remove
     */
    void removeFromBacklog(String userEmail, UUID videoGameId);

    /**
     * Returns the authenticated user's backlog (paginated).
     *
     * @param userEmail the authenticated user's email
     * @param pageable  pagination parameters
     * @return paginated list of backlog games
     */
    Page<BacklogResponseDto> getUserBacklog(String userEmail, Pageable pageable);

    /**
     * Checks if a game is in the authenticated user's backlog.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID to check
     * @return true if the game is in the backlog
     */
    boolean isInBacklog(String userEmail, UUID videoGameId);

    /**
     * Sets or clears the priority on the authenticated user's backlog entry for a game.
     *
     * @param userEmail   the authenticated user's email
     * @param videoGameId the video game ID
     * @param priority    the new priority, or {@code null} to clear it
     * @return the updated backlog entry
     */
    BacklogResponseDto updatePriority(String userEmail, UUID videoGameId, Priority priority);
}
