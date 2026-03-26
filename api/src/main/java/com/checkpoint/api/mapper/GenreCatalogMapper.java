package com.checkpoint.api.mapper;

import java.util.List;

import com.checkpoint.api.dto.catalog.GenreCatalogDto;
import com.checkpoint.api.entities.Genre;

/**
 * Mapper for converting {@link Genre} entities to catalog DTOs.
 */
public interface GenreCatalogMapper {

    /**
     * Converts a Genre entity to a catalog DTO.
     *
     * @param genre the entity to convert
     * @return the catalog DTO
     */
    GenreCatalogDto toDto(Genre genre);

    /**
     * Converts a list of Genre entities to catalog DTOs.
     *
     * @param genres the entities to convert
     * @return the list of catalog DTOs
     */
    List<GenreCatalogDto> toDtoList(List<Genre> genres);
}
