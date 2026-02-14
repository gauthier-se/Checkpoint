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
}
