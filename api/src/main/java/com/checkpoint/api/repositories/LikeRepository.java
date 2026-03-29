package com.checkpoint.api.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.Like;

/**
 * Repository for Like entity.
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {

    /**
     * Counts the number of likes for a game list.
     */
    long countByGameListId(UUID gameListId);

    /**
     * Checks if a user has liked a game list.
     */
    boolean existsByUserIdAndGameListId(UUID userId, UUID gameListId);
}
