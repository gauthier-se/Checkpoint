package com.checkpoint.api.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.Tag;
import com.checkpoint.api.entities.UserGamePlay;

/**
 * Repository for {@link Tag} entity.
 * Provides user-scoped tag queries and play log count projections.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Finds all tags belonging to a user.
     *
     * @param userId the user ID
     * @return list of tags ordered by name
     */
    List<Tag> findByUserIdOrderByNameAsc(UUID userId);

    /**
     * Finds all tags belonging to a user by their username (public access).
     *
     * @param pseudo the username
     * @return list of tags ordered by name
     */
    List<Tag> findByUserPseudoOrderByNameAsc(String pseudo);

    /**
     * Finds a tag by ID and user ID (ownership check).
     *
     * @param id     the tag ID
     * @param userId the user ID
     * @return the tag if found and owned by the user
     */
    Optional<Tag> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Finds a tag by name and user username (public lookup).
     *
     * @param name   the tag name (normalized)
     * @param pseudo the username
     * @return the tag if found
     */
    Optional<Tag> findByNameAndUserPseudo(String name, String pseudo);

    /**
     * Checks if a user already has a tag with the given name (case-insensitive).
     *
     * @param userId the user ID
     * @param name   the tag name
     * @return true if a tag with that name exists for the user
     */
    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);

    /**
     * Finds all tags by their IDs that belong to a specific user.
     *
     * @param ids    the tag IDs
     * @param userId the user ID
     * @return list of matching tags owned by the user
     */
    List<Tag> findAllByIdInAndUserId(List<UUID> ids, UUID userId);

    /**
     * Returns paginated play logs associated with a specific tag.
     *
     * @param tagId    the tag ID
     * @param pageable pagination parameters
     * @return page of play logs
     */
    @Query("""
            SELECT p FROM UserGamePlay p
            JOIN FETCH p.videoGame
            JOIN FETCH p.platform
            JOIN p.tags t
            WHERE t.id = :tagId
            """)
    Page<UserGamePlay> findPlayLogsByTagId(@Param("tagId") UUID tagId, Pageable pageable);

    /**
     * Returns paginated play logs for a tag identified by name and user username (public).
     *
     * @param tagName  the tag name (normalized)
     * @param pseudo   the username
     * @param pageable pagination parameters
     * @return page of play logs
     */
    @Query("""
            SELECT p FROM UserGamePlay p
            JOIN FETCH p.videoGame
            JOIN FETCH p.platform
            JOIN p.tags t
            WHERE t.name = :tagName AND t.user.pseudo = :pseudo
            """)
    Page<UserGamePlay> findPlayLogsByTagNameAndUserPseudo(
            @Param("tagName") String tagName,
            @Param("pseudo") String pseudo,
            Pageable pageable
    );
}
