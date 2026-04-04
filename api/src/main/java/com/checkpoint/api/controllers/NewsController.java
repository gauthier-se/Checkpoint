package com.checkpoint.api.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.catalog.NewsResponseDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.services.NewsService;

/**
 * REST controller for public news endpoints.
 * Provides read-only access to published news articles.
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private static final Logger log = LoggerFactory.getLogger(NewsController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "publishedAt,desc";

    private final NewsService newsService;

    /**
     * Constructs a new NewsController.
     *
     * @param newsService the news service
     */
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * Retrieves a paginated list of published news articles.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sorting parameters
     * @return the paginated published news articles
     */
    @GetMapping
    public ResponseEntity<PagedResponseDto<NewsResponseDto>> getPublishedNews(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

        log.info("GET /api/news - page: {}, size: {}, sort: {}", page, size, sort);

        int validatedSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validatedPage = Math.max(0, page);

        Pageable pageable = createPageable(validatedPage, validatedSize, sort);
        Page<NewsResponseDto> newsPage = newsService.getPublishedNews(pageable);

        return ResponseEntity.ok(PagedResponseDto.from(newsPage));
    }

    /**
     * Retrieves a single published news article by ID.
     *
     * @param newsId the news article ID
     * @return the published news article
     */
    @GetMapping("/{newsId}")
    public ResponseEntity<NewsResponseDto> getNewsById(@PathVariable UUID newsId) {
        log.info("GET /api/news/{}", newsId);

        NewsResponseDto news = newsService.getNewsById(newsId);

        return ResponseEntity.ok(news);
    }

    /**
     * Creates a Pageable from the sort string.
     * Supports format: "field,direction" (e.g., "publishedAt,desc").
     *
     * @param page the page number
     * @param size the page size
     * @param sort the sort string
     * @return a Pageable instance
     */
    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0].trim();
        Sort.Direction direction = sortParts.length > 1
                && sortParts[1].trim().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String mappedField = mapSortField(sortField);

        return PageRequest.of(page, size, Sort.by(direction, mappedField));
    }

    /**
     * Maps API sort field names to entity field names.
     *
     * @param field the API field name
     * @return the entity field name
     */
    private String mapSortField(String field) {
        return switch (field.toLowerCase()) {
            case "publishedat", "published_at" -> "publishedAt";
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            default -> "publishedAt";
        };
    }
}
