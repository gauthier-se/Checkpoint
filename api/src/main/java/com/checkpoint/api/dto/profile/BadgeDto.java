package com.checkpoint.api.dto.profile;

import java.util.UUID;

/**
 * DTO representing a badge earned by a user.
 *
 * @param id          the badge ID
 * @param name        the badge name
 * @param picture     the badge icon URL
 * @param description the badge description
 */
public record BadgeDto(
        UUID id,
        String name,
        String picture,
        String description
) {}
