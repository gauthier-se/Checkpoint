package com.checkpoint.api.dto.catalog;

/**
 * Request DTO for creating or updating a news article.
 *
 * @param title       the news title
 * @param description the news content
 * @param picture     the cover image URL
 */
public record NewsRequestDto(
        String title,
        String description,
        String picture
) {}
