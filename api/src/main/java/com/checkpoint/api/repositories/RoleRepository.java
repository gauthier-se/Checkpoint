package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checkpoint.api.entities.Role;

/**
 * Repository for {@link Role} entity.
 */
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Finds a role by its name.
     *
     * @param name the role name
     * @return an optional containing the role if found
     */
    Optional<Role> findByName(String name);
}
