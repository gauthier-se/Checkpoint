package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.checkpoint.api.entities.Review;

/**
 * Repository for {@link Review} entity.
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Finds a user's review for a specific video game.
     *
     * @param pseudo the user's pseudo
     * @param videoGameId the video game ID
     * @return an optional containing the review if found
     */
    Optional<Review> findByUserPseudoAndVideoGameId(String pseudo, UUID videoGameId);

    /**
     * Finds all reviews for a specific video game.
     *
     * @param videoGameId the video game ID
     * @param pageable pagination and sorting details
     * @return a page of reviews
     */
    Page<Review> findByVideoGameId(UUID videoGameId, Pageable pageable);

}
