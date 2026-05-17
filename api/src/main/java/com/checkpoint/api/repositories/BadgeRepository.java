package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checkpoint.api.entities.Badge;

/**
 * Repository for {@link Badge} entity.
 */
public interface BadgeRepository extends JpaRepository<Badge, UUID> {

    /**
     * Finds a badge by its stable catalog code.
     *
     * @param code the badge code (matches a {@link com.checkpoint.api.enums.BadgeCode} name)
     * @return an optional containing the badge if found
     */
    Optional<Badge> findByCode(String code);
}
