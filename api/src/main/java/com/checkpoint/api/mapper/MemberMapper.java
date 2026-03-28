package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.social.MemberCardDto;
import com.checkpoint.api.entities.User;

/**
 * Mapper for converting User entities to member discovery DTOs.
 */
public interface MemberMapper {

    /**
     * Converts a User entity to a MemberCardDto with precomputed counts.
     *
     * @param user          the user entity
     * @param followerCount the number of followers
     * @param reviewCount   the number of reviews
     * @param isFollowing   whether the viewer follows this user (null if not authenticated)
     * @return the member card DTO
     */
    MemberCardDto toMemberCardDto(User user, Long followerCount, Long reviewCount, Boolean isFollowing);
}
