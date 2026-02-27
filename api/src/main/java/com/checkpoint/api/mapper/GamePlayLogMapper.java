package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.playlog.GamePlayLogRequestDto;
import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.entities.UserGamePlay;

/**
 * Mapper for {@link UserGamePlay} entities and DTOs.
 */
public interface GamePlayLogMapper {

    /**
     * Maps a UserGamePlay entity to a GamePlayLogResponseDto.
     */
    GamePlayLogResponseDto toDto(UserGamePlay playLog);

    /**
     * Maps a GamePlayLogRequestDto to a new UserGamePlay entity.
     * Note: References (User, VideoGame, Platform) must be resolved separately.
     */
    UserGamePlay toEntity(GamePlayLogRequestDto request);

    /**
     * Updates an existing UserGamePlay entity with data from a request DTO.
     * Overwrites non-null fields only, or all fields depending on requirements.
     */
    void updateEntityFromDto(GamePlayLogRequestDto request, UserGamePlay playLog);
}
