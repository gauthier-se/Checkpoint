package com.checkpoint.api.dto.catalog;

import java.util.UUID;

/**
 * DTO for platform catalog listing.
 *
 * @param id              the platform ID
 * @param name            the platform name
 * @param videoGamesCount the number of video games on this platform
 */
public record PlatformCatalogDto(
        UUID id,
        String name,
        Integer videoGamesCount
) {}
