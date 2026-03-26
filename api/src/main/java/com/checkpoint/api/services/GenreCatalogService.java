package com.checkpoint.api.services;

import java.util.List;

import com.checkpoint.api.dto.catalog.GenreCatalogDto;

/**
 * Service interface for genre catalog operations.
 * Provides methods for retrieving genres for public display.
 */
public interface GenreCatalogService {

    /**
     * Retrieves all genres sorted alphabetically by name.
     *
     * @return list of genre catalog DTOs
     */
    List<GenreCatalogDto> getAllGenres();
}
