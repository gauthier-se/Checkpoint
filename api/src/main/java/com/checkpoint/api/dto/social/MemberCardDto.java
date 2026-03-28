package com.checkpoint.api.dto.social;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing a member card in discovery listings.
 *
 * @param id            the user's UUID
 * @param pseudo        the user's display name
 * @param picture       the user's profile picture URL
 * @param level         the user's level
 * @param followerCount the number of followers
 * @param reviewCount   the number of reviews written
 * @param isFollowing   whether the authenticated viewer follows this user (null if not authenticated)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MemberCardDto(
        UUID id,
        String pseudo,
        String picture,
        Integer level,
        Long followerCount,
        Long reviewCount,
        Boolean isFollowing
) {}
