package com.checkpoint.api.dto.catalog;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for game cards displayed in the catalog list.
 * Contains minimal information for displaying games in a grid/list view.
 */
public record GameCardDto(
        UUID id,
        String title,
        String coverUrl,
        LocalDate releaseDate,
        Double averageRating,
        Long ratingCount
) {
    /**
     * Constructor for JPA projection.
     */
    public GameCardDto(UUID id, String title, String coverUrl, LocalDate releaseDate,
                       Double averageRating, Long ratingCount) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.releaseDate = releaseDate;
        this.averageRating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : null;
        this.ratingCount = ratingCount != null ? ratingCount : 0L;
    }
}
