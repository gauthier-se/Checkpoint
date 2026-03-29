package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.list.AddGameToListRequestDto;
import com.checkpoint.api.dto.list.CreateGameListRequestDto;
import com.checkpoint.api.dto.list.GameListCardDto;
import com.checkpoint.api.dto.list.GameListDetailDto;
import com.checkpoint.api.dto.list.ReorderGamesRequestDto;
import com.checkpoint.api.dto.list.UpdateGameListRequestDto;

/**
 * Service for managing game lists.
 */
public interface GameListService {

    /**
     * Creates a new game list for the authenticated user.
     *
     * @param userEmail the authenticated user's email
     * @param request   the creation request
     * @return the created list as a detail DTO
     */
    GameListDetailDto createList(String userEmail, CreateGameListRequestDto request);

    /**
     * Updates an existing game list. Only the owner can update.
     *
     * @param userEmail the authenticated user's email
     * @param listId    the list ID
     * @param request   the update request
     * @return the updated list as a detail DTO
     */
    GameListDetailDto updateList(String userEmail, UUID listId, UpdateGameListRequestDto request);

    /**
     * Deletes a game list. Only the owner can delete.
     *
     * @param userEmail the authenticated user's email
     * @param listId    the list ID
     */
    void deleteList(String userEmail, UUID listId);

    /**
     * Retrieves all lists owned by the authenticated user.
     *
     * @param userEmail the authenticated user's email
     * @param pageable  pagination parameters
     * @return a page of list card DTOs
     */
    Page<GameListCardDto> getUserLists(String userEmail, Pageable pageable);

    /**
     * Adds a video game to a list. Only the owner can add games.
     *
     * @param userEmail the authenticated user's email
     * @param listId    the list ID
     * @param request   the add game request
     * @return the updated list as a detail DTO
     */
    GameListDetailDto addGameToList(String userEmail, UUID listId, AddGameToListRequestDto request);

    /**
     * Removes a video game from a list. Only the owner can remove games.
     *
     * @param userEmail   the authenticated user's email
     * @param listId      the list ID
     * @param videoGameId the video game ID to remove
     */
    void removeGameFromList(String userEmail, UUID listId, UUID videoGameId);

    /**
     * Reorders games in a list. Only the owner can reorder.
     *
     * @param userEmail the authenticated user's email
     * @param listId    the list ID
     * @param request   the reorder request with ordered video game IDs
     * @return the updated list as a detail DTO
     */
    GameListDetailDto reorderGames(String userEmail, UUID listId, ReorderGamesRequestDto request);

    /**
     * Retrieves recent public lists.
     *
     * @param pageable pagination parameters
     * @return a page of list card DTOs
     */
    Page<GameListCardDto> getRecentPublicLists(Pageable pageable);

    /**
     * Retrieves popular public lists (sorted by like count).
     *
     * @param pageable pagination parameters
     * @return a page of list card DTOs
     */
    Page<GameListCardDto> getPopularPublicLists(Pageable pageable);

    /**
     * Retrieves a list detail by ID. Private lists are only accessible to the owner.
     *
     * @param listId      the list ID
     * @param viewerEmail the viewer's email, or null if anonymous
     * @return the list detail DTO
     */
    GameListDetailDto getListDetail(UUID listId, String viewerEmail);

    /**
     * Retrieves public lists for a user profile.
     *
     * @param username the profile owner's username (pseudo)
     * @param pageable pagination parameters
     * @return a page of list card DTOs
     */
    Page<GameListCardDto> getUserPublicLists(String username, Pageable pageable);
}
