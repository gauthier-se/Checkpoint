package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.dto.catalog.GameCardDto;
import com.checkpoint.api.entities.VideoGame;

/**
 * Repository for VideoGame entity.
 */
@Repository
public interface VideoGameRepository extends JpaRepository<VideoGame, UUID> {

    /**
     * Finds a video game by its IGDB external ID.
     * Used for duplicate detection during import.
     *
     * @param igdbId the IGDB game ID
     * @return Optional containing the video game if found
     */
    Optional<VideoGame> findByIgdbId(Long igdbId);

    /**
     * Checks if a video game with the given IGDB ID exists.
     *
     * @param igdbId the IGDB game ID
     * @return true if exists, false otherwise
     */
    boolean existsByIgdbId(Long igdbId);

    /**
     * Fetches a paginated list of games as GameCardDto projections.
     * Uses a single query with aggregated rating calculation to avoid N+1 issues.
     *
     * @param pageable pagination and sorting parameters
     * @return page of GameCardDto
     */
    @Query("""
            SELECT new com.checkpoint.api.dto.catalog.GameCardDto(
                vg.id,
                vg.title,
                vg.coverUrl,
                vg.releaseDate,
                AVG(CAST(r.score AS double)),
                COUNT(r.id)
            )
            FROM VideoGame vg
            LEFT JOIN vg.rates r
            GROUP BY vg.id, vg.title, vg.coverUrl, vg.releaseDate
            """)
    Page<GameCardDto> findAllAsGameCards(Pageable pageable);

    /**
     * Fetches a video game with all its relationships eagerly loaded.
     * Uses JOIN FETCH to avoid N+1 issues when accessing genres, platforms, and companies.
     *
     * @param id the video game ID
     * @return Optional containing the video game with relationships loaded
     */
    @Query("""
            SELECT vg FROM VideoGame vg
            LEFT JOIN FETCH vg.genres
            LEFT JOIN FETCH vg.platforms
            LEFT JOIN FETCH vg.companies
            WHERE vg.id = :id
            """)
    Optional<VideoGame> findByIdWithRelationships(@Param("id") UUID id);

    /**
     * Calculates the average rating for a video game.
     *
     * @param videoGameId the video game ID
     * @return average rating or null if no ratings
     */
    @Query("SELECT AVG(CAST(r.score AS double)) FROM Rate r WHERE r.videoGame.id = :videoGameId")
    Double calculateAverageRating(@Param("videoGameId") UUID videoGameId);

    /**
     * Counts the number of ratings for a video game.
     *
     * @param videoGameId the video game ID
     * @return count of ratings
     */
    @Query("SELECT COUNT(r) FROM Rate r WHERE r.videoGame.id = :videoGameId")
    Long countRatings(@Param("videoGameId") UUID videoGameId);
}
