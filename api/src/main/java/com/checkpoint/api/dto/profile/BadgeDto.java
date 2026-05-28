package com.checkpoint.api.dto.profile;

import java.util.UUID;

/**
 * DTO representing a badge in the profile view.
 *
 * <p>The profile endpoint returns the full catalog so the client can render
 * silhouettes for unearned hidden badges. The {@code earned} flag tells the
 * client which badges the profile owner currently holds.</p>
 *
 * @param id          the badge ID
 * @param code        the stable catalog code (e.g. {@code KONAMI})
 * @param name        the badge display name
 * @param picture     the badge icon URL
 * @param description the badge description
 * @param hidden      whether this is an easter-egg badge (rendered as a
 *                    silhouette + {@code ???} when not yet earned)
 * @param earned      whether the profile owner has earned this badge
 */
public record BadgeDto(
        UUID id,
        String code,
        String name,
        String picture,
        String description,
        boolean hidden,
        boolean earned
) {}
