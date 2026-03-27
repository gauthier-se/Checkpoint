package com.checkpoint.api.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.catalog.GameCardDto;

/**
 * Custom repository interface for dynamic filtering of video games.
 * Provides methods that build JPQL queries dynamically based on optional filter parameters.
 */
public interface VideoGameRepositoryCustom {

    /**
     * Fetches a paginated list of games as GameCardDto projections with optional filters.
     * When no filters are provided, behavior is identical to the standard findAllAsGameCards query.
     *
     * @param pageable   pagination and sorting parameters
     * @param genre      optional genre name filter (case-insensitive exact match)
     * @param platform   optional platform name filter (case-insensitive exact match)
     * @param yearMin    optional minimum release year (inclusive)
     * @param yearMax    optional maximum release year (inclusive)
     * @param ratingMin  optional minimum average rating (inclusive)
     * @param ratingMax  optional maximum average rating (inclusive)
     * @return page of GameCardDto matching the filters
     */
    Page<GameCardDto> findAllAsGameCardsWithFilters(Pageable pageable,
                                                     String genre,
                                                     String platform,
                                                     Integer yearMin,
                                                     Integer yearMax,
                                                     Double ratingMin,
                                                     Double ratingMax);
}
