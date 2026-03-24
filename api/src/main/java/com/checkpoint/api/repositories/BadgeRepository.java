package com.checkpoint.api.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checkpoint.api.entities.Badge;

/**
 * Repository for {@link Badge} entity.
 */
public interface BadgeRepository extends JpaRepository<Badge, UUID> {
}
