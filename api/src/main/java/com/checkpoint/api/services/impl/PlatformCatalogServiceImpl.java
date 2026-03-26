package com.checkpoint.api.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.catalog.PlatformCatalogDto;
import com.checkpoint.api.entities.Platform;
import com.checkpoint.api.mapper.PlatformCatalogMapper;
import com.checkpoint.api.repositories.PlatformRepository;
import com.checkpoint.api.services.PlatformCatalogService;

/**
 * Implementation of {@link PlatformCatalogService}.
 * Retrieves platforms sorted alphabetically for catalog display.
 */
@Service
@Transactional(readOnly = true)
public class PlatformCatalogServiceImpl implements PlatformCatalogService {

    private static final Logger log = LoggerFactory.getLogger(PlatformCatalogServiceImpl.class);

    private final PlatformRepository platformRepository;
    private final PlatformCatalogMapper platformCatalogMapper;

    public PlatformCatalogServiceImpl(PlatformRepository platformRepository,
                                      PlatformCatalogMapper platformCatalogMapper) {
        this.platformRepository = platformRepository;
        this.platformCatalogMapper = platformCatalogMapper;
    }

    @Override
    public List<PlatformCatalogDto> getAllPlatforms() {
        log.debug("Fetching all platforms sorted by name");

        List<Platform> platforms = platformRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return platformCatalogMapper.toDtoList(platforms);
    }
}
