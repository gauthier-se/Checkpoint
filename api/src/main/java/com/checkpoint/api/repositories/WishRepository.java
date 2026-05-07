package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.Wish;

/**
 * Repository for {@link Wish} entities.
 */
@Repository
public interface WishRepository extends JpaRepository<Wish, UUID> {

    /**
     * Checks if a user already has a specific game in their wishlist.
     */
    boolean existsByUserIdAndVideoGameId(UUID userId, UUID videoGameId);

    /**
     * Finds a wish by user ID and video game ID.
     */
    Optional<Wish> findByUserIdAndVideoGameId(UUID userId, UUID videoGameId);

    /**
     * Returns all games in a user's wishlist (paginated), with video game eagerly fetched.
     */
    @Query("""
            SELECT w FROM Wish w
            JOIN FETCH w.videoGame
            WHERE w.user.id = :userId
            """)
    Page<Wish> findByUserIdWithVideoGame(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Returns the user's wishlist ordered by priority descending: HIGH → MEDIUM → LOW → NULL.
     * Matches {@code ?sort=priority,desc}. Pageable's own sort is ignored.
     */
    @Query("""
            SELECT w FROM Wish w
            JOIN FETCH w.videoGame
            WHERE w.user.id = :userId
            ORDER BY CASE w.priority
                       WHEN com.checkpoint.api.enums.Priority.HIGH   THEN 3
                       WHEN com.checkpoint.api.enums.Priority.MEDIUM THEN 2
                       WHEN com.checkpoint.api.enums.Priority.LOW    THEN 1
                       ELSE 0
                     END DESC
            """)
    Page<Wish> findByUserIdWithVideoGameOrderByPriorityDesc(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Returns the user's wishlist ordered by priority ascending: NULL → LOW → MEDIUM → HIGH.
     * Matches {@code ?sort=priority,asc}. Pageable's own sort is ignored.
     */
    @Query("""
            SELECT w FROM Wish w
            JOIN FETCH w.videoGame
            WHERE w.user.id = :userId
            ORDER BY CASE w.priority
                       WHEN com.checkpoint.api.enums.Priority.HIGH   THEN 3
                       WHEN com.checkpoint.api.enums.Priority.MEDIUM THEN 2
                       WHEN com.checkpoint.api.enums.Priority.LOW    THEN 1
                       ELSE 0
                     END
            """)
    Page<Wish> findByUserIdWithVideoGameOrderByPriorityAsc(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Returns all games in a user's wishlist by pseudo (paginated), with video game eagerly fetched.
     *
     * @param pseudo   the user's pseudo
     * @param pageable pagination parameters
     * @return a page of wishes
     */
    @Query("""
            SELECT w FROM Wish w
            JOIN FETCH w.videoGame
            WHERE w.user.pseudo = :pseudo
            """)
    Page<Wish> findByUserPseudoWithVideoGame(@Param("pseudo") String pseudo, Pageable pageable);

    /**
     * Counts the number of wishes for a user with the given pseudo.
     *
     * @param pseudo the user's pseudo
     * @return the wish count
     */
    long countByUserPseudo(String pseudo);

    /**
     * Deletes a wish by user ID and video game ID.
     */
    void deleteByUserIdAndVideoGameId(UUID userId, UUID videoGameId);

    /**
     * Counts the number of users who have wishlisted a specific game.
     */
    long countByVideoGameId(UUID videoGameId);
}
