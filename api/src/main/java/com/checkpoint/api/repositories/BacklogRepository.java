package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.Backlog;

/**
 * Repository for {@link Backlog} entities.
 */
@Repository
public interface BacklogRepository extends JpaRepository<Backlog, UUID> {

    /**
     * Checks if a user already has a specific game in their backlog.
     */
    boolean existsByUserIdAndVideoGameId(UUID userId, UUID videoGameId);

    /**
     * Finds a backlog entry by user ID and video game ID.
     */
    Optional<Backlog> findByUserIdAndVideoGameId(UUID userId, UUID videoGameId);

    /**
     * Returns all games in a user's backlog (paginated), with video game eagerly fetched.
     */
    @Query("""
            SELECT b FROM Backlog b
            JOIN FETCH b.videoGame
            WHERE b.user.id = :userId
            """)
    Page<Backlog> findByUserIdWithVideoGame(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Deletes a backlog entry by user ID and video game ID.
     */
    void deleteByUserIdAndVideoGameId(UUID userId, UUID videoGameId);

    /**
     * Counts the number of users who have a specific game in their backlog.
     */
    long countByVideoGameId(UUID videoGameId);
}
