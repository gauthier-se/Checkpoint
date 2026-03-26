package com.checkpoint.api.services;

import java.util.List;

import com.checkpoint.api.dto.catalog.PlatformCatalogDto;

/**
 * Service interface for platform catalog operations.
 * Provides methods for retrieving platforms for public display.
 */
public interface PlatformCatalogService {

    /**
     * Retrieves all platforms sorted alphabetically by name.
     *
     * @return list of platform catalog DTOs
     */
    List<PlatformCatalogDto> getAllPlatforms();
}
