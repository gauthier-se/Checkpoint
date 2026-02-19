package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.collection.UserGameResponseDto;
import com.checkpoint.api.entities.UserGame;

/**
 * Mapper for converting {@link UserGame} entities to DTOs.
 */
public interface UserGameMapper {

    /**
     * Converts a UserGame entity to a response DTO.
     *
     * @param userGame the entity to convert
     * @return the response DTO
     */
    UserGameResponseDto toResponseDto(UserGame userGame);
}
