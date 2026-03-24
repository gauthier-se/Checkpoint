package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.checkpoint.api.entities.User;

/**
 * Repository for {@link User} entity.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address
     * @return an optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists by their email address.
     *
     * @param email the email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists by their pseudo.
     *
     * @param pseudo the pseudo
     * @return true if exists, false otherwise
     */
    boolean existsByPseudo(String pseudo);

    /**
     * Finds all users who follow the given user (paginated).
     *
     * @param userId   the ID of the user whose followers to retrieve
     * @param pageable pagination parameters
     * @return a page of users who follow the given user
     */
    @Query("SELECT u FROM User u JOIN u.following f WHERE f.id = :userId")
    Page<User> findFollowersByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds all users that the given user follows (paginated).
     *
     * @param userId   the ID of the user whose following list to retrieve
     * @param pageable pagination parameters
     * @return a page of users that the given user follows
     */
    @Query("SELECT f FROM User u JOIN u.following f WHERE u.id = :userId")
    Page<User> findFollowingByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds a user by their pseudo (display name).
     *
     * @param pseudo the pseudo
     * @return an optional containing the user if found
     */
    Optional<User> findByPseudo(String pseudo);

    /**
     * Finds a user by their pseudo, eagerly fetching badges.
     *
     * @param pseudo the pseudo
     * @return an optional containing the user with badges loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.badges WHERE u.pseudo = :pseudo")
    Optional<User> findByPseudoWithBadges(@Param("pseudo") String pseudo);

    /**
     * Counts the number of followers for a given user.
     *
     * @param userId the user's ID
     * @return the follower count
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.following f WHERE f.id = :userId")
    long countFollowersByUserId(@Param("userId") UUID userId);

    /**
     * Counts the number of users that the given user follows.
     *
     * @param userId the user's ID
     * @return the following count
     */
    @Query("SELECT COUNT(f) FROM User u JOIN u.following f WHERE u.id = :userId")
    long countFollowingByUserId(@Param("userId") UUID userId);

    /**
     * Checks if a follower is following a target user.
     *
     * @param followerId  the ID of the potential follower
     * @param followingId the ID of the user potentially being followed
     * @return true if the follower is following the target user
     */
    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.following f WHERE u.id = :followerId AND f.id = :followingId")
    boolean isFollowing(@Param("followerId") UUID followerId, @Param("followingId") UUID followingId);
}
