package com.checkpoint.api.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.playlog.GamePlayLogRequestDto;
import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;

/**
 * Service for managing user game play logs (diary).
 */
public interface GamePlayLogService {

    /**
     * Logs a new play session.
     */
    GamePlayLogResponseDto logPlay(String userEmail, GamePlayLogRequestDto request);

    /**
     * Updates an existing play session.
     */
    GamePlayLogResponseDto updatePlayLog(String userEmail, UUID playId, GamePlayLogRequestDto request);

    /**
     * Deletes a play session. The session must belong to the user.
     */
    void deletePlayLog(String userEmail, UUID playId);

    /**
     * Retrieves all play sessions for the user (paginated).
     */
    Page<GamePlayLogResponseDto> getUserPlayLog(String userEmail, Pageable pageable);

    /**
     * Retrieves all play sessions by the user for a specific game.
     */
    List<GamePlayLogResponseDto> getGamePlayHistory(String userEmail, UUID videoGameId);
}
