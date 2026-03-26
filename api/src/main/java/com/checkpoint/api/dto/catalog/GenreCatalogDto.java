package com.checkpoint.api.dto.catalog;

import java.util.UUID;

/**
 * DTO for genre catalog listing.
 *
 * @param id              the genre ID
 * @param name            the genre name
 * @param videoGamesCount the number of video games in this genre
 */
public record GenreCatalogDto(
        UUID id,
        String name,
        Integer videoGamesCount
) {}
