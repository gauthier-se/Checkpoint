package com.checkpoint.api.dto.catalog;

import java.util.UUID;

/**
 * DTO representing a news article author.
 *
 * @param id      the author's user ID
 * @param pseudo  the author's username
 * @param picture the author's profile picture URL
 */
public record NewsAuthorDto(
        UUID id,
        String pseudo,
        String picture
) {}
