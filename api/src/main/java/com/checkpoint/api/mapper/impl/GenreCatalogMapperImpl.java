package com.checkpoint.api.mapper.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.catalog.GenreCatalogDto;
import com.checkpoint.api.entities.Genre;
import com.checkpoint.api.mapper.GenreCatalogMapper;

/**
 * Implementation of {@link GenreCatalogMapper}.
 */
@Component
public class GenreCatalogMapperImpl implements GenreCatalogMapper {

    @Override
    public GenreCatalogDto toDto(Genre genre) {
        return new GenreCatalogDto(
                genre.getId(),
                genre.getName(),
                genre.getVideoGamesCount()
        );
    }

    @Override
    public List<GenreCatalogDto> toDtoList(List<Genre> genres) {
        return genres.stream()
                .map(this::toDto)
                .toList();
    }
}
