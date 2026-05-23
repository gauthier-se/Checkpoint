package com.checkpoint.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.list.GameListCardDto;
import com.checkpoint.api.dto.list.GameListSearchCriteria;

/**
 * Full-text search service for game lists, backed by Hibernate Search / Lucene.
 */
public interface ListSearchService {

    /**
     * Paginated search with optional fuzzy text query, filters, and sort.
     * When the criteria has no text query, this behaves like a paginated listing
     * with Lucene-backed sorting and filters.
     *
     * @param criteria    the filter / sort criteria (nullable fields are ignored)
     * @param pageable    pagination parameters
     * @param viewerEmail email of the authenticated viewer, or null if anonymous.
     *                    Required for {@code visibility=mine}; ignored otherwise.
     * @return a page of mapped list card DTOs
     */
    Page<GameListCardDto> search(GameListSearchCriteria criteria, Pageable pageable, String viewerEmail);
}
