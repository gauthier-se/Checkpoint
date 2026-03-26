package com.checkpoint.api.mapper;

import java.util.List;

import com.checkpoint.api.dto.catalog.PlatformCatalogDto;
import com.checkpoint.api.entities.Platform;

/**
 * Mapper for converting {@link Platform} entities to catalog DTOs.
 */
public interface PlatformCatalogMapper {

    /**
     * Converts a Platform entity to a catalog DTO.
     *
     * @param platform the entity to convert
     * @return the catalog DTO
     */
    PlatformCatalogDto toDto(Platform platform);

    /**
     * Converts a list of Platform entities to catalog DTOs.
     *
     * @param platforms the entities to convert
     * @return the list of catalog DTOs
     */
    List<PlatformCatalogDto> toDtoList(List<Platform> platforms);
}
