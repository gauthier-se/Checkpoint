package com.checkpoint.api.dto.catalog;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Generic DTO for paginated responses.
 * Wraps Spring's Page with a cleaner API for frontend consumption.
 *
 * @param <T> the type of content in the page
 */
public record PagedResponseDto<T>(
        List<T> content,
        PageMetadata metadata
) {
    /**
     * Metadata about the page.
     */
    public record PageMetadata(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean hasNext,
            boolean hasPrevious
    ) {}

    /**
     * Creates a PagedResponseDto from a Spring Page.
     *
     * @param page the Spring Page
     * @param <T> the type of content
     * @return a PagedResponseDto
     */
    public static <T> PagedResponseDto<T> from(Page<T> page) {
        PageMetadata metadata = new PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
        return new PagedResponseDto<>(page.getContent(), metadata);
    }
}
