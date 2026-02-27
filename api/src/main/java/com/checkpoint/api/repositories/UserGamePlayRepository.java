package com.checkpoint.api.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.UserGamePlay;

/**
 * Repository for {@link UserGamePlay} entities.
 */
@Repository
public interface UserGamePlayRepository extends JpaRepository<UserGamePlay, UUID> {

    /**
     * Returns all play logs for a user (paginated), with video game and platform eagerly fetched.
     */
    @Query("""
            SELECT p FROM UserGamePlay p
            JOIN FETCH p.videoGame
            JOIN FETCH p.platform
            WHERE p.user.id = :userId
            """)
    Page<UserGamePlay> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Returns all play entries for a specific user and game.
     */
    @Query("""
            SELECT p FROM UserGamePlay p
            JOIN FETCH p.videoGame
            JOIN FETCH p.platform
            WHERE p.user.id = :userId AND p.videoGame.id = :videoGameId
            """)
    List<UserGamePlay> findByUserIdAndVideoGameId(@Param("userId") UUID userId, @Param("videoGameId") UUID videoGameId);

    /**
     * Counts the number of play entries for a specific user and game.
     */
    long countByUserIdAndVideoGameId(UUID userId, UUID videoGameId);
}
