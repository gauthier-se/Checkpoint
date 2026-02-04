package com.checkpoint.api.dto.admin;

/**
 * Lightweight DTO for external game search results.
 * Contains minimal information for displaying search results before import.
 */
public record ExternalGameDto(
        Long externalId,
        String title,
        Integer releaseYear,
        String coverUrl
) {
    /**
     * Creates an ExternalGameDto from IGDB game data.
     *
     * @param id the IGDB game ID
     * @param name the game title
     * @param firstReleaseDate Unix timestamp of first release
     * @param coverUrl the cover image URL
     * @return ExternalGameDto instance
     */
    public static ExternalGameDto fromIgdb(Long id, String name, Long firstReleaseDate, String coverUrl) {
        Integer year = null;
        if (firstReleaseDate != null) {
            // Convert Unix timestamp to year
            year = java.time.Instant.ofEpochSecond(firstReleaseDate)
                    .atZone(java.time.ZoneId.systemDefault())
                    .getYear();
        }
        return new ExternalGameDto(id, name, year, coverUrl);
    }
}
