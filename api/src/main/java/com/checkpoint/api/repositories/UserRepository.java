package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
