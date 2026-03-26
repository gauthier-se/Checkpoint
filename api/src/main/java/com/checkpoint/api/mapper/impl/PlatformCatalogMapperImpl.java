package com.checkpoint.api.mapper.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.catalog.PlatformCatalogDto;
import com.checkpoint.api.entities.Platform;
import com.checkpoint.api.mapper.PlatformCatalogMapper;

/**
 * Implementation of {@link PlatformCatalogMapper}.
 */
@Component
public class PlatformCatalogMapperImpl implements PlatformCatalogMapper {

    @Override
    public PlatformCatalogDto toDto(Platform platform) {
        return new PlatformCatalogDto(
                platform.getId(),
                platform.getName(),
                platform.getVideoGamesCount()
        );
    }

    @Override
    public List<PlatformCatalogDto> toDtoList(List<Platform> platforms) {
        return platforms.stream()
                .map(this::toDto)
                .toList();
    }
}
