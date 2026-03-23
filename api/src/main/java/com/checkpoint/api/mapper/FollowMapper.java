package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.social.FollowUserDto;
import com.checkpoint.api.entities.User;

/**
 * Mapper for converting User entities to follow-related DTOs.
 */
public interface FollowMapper {

    /**
     * Converts a User entity to a FollowUserDto.
     *
     * @param user the user entity
     * @return the follow user DTO
     */
    FollowUserDto toFollowUserDto(User user);
}
