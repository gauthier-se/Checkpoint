package com.checkpoint.api.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.GenreCatalogDto;
import com.checkpoint.api.entities.Genre;
import com.checkpoint.api.mapper.GenreCatalogMapper;
import com.checkpoint.api.repositories.GenreRepository;
import com.checkpoint.api.services.GenreCatalogService;

/**
 * Implementation of {@link GenreCatalogService}.
 * Retrieves genres sorted alphabetically for catalog display.
 */
@Service
@Transactional(readOnly = true)
public class GenreCatalogServiceImpl implements GenreCatalogService {

    private static final Logger log = LoggerFactory.getLogger(GenreCatalogServiceImpl.class);

    private final GenreRepository genreRepository;
    private final GenreCatalogMapper genreCatalogMapper;

    public GenreCatalogServiceImpl(GenreRepository genreRepository,
                                   GenreCatalogMapper genreCatalogMapper) {
        this.genreRepository = genreRepository;
        this.genreCatalogMapper = genreCatalogMapper;
    }

    @Override
    public List<GenreCatalogDto> getAllGenres() {
        log.debug("Fetching all genres sorted by name");

        List<Genre> genres = genreRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return genreCatalogMapper.toDtoList(genres);
    }
}
