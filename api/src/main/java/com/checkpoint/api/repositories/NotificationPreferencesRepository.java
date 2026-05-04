package com.checkpoint.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.NotificationPreferences;

/**
 * Repository for {@link NotificationPreferences} entities.
 */
@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, UUID> {

    /**
     * Finds the notification preferences row for the given user ID, if any.
     *
     * @param userId the owning user's ID
     * @return the preferences row, or empty if the user has not customized them yet
     */
    Optional<NotificationPreferences> findByUserId(UUID userId);

    /**
     * Finds the notification preferences row for the given user email, if any.
     *
     * @param email the owning user's email
     * @return the preferences row, or empty if the user has not customized them yet
     */
    Optional<NotificationPreferences> findByUserEmail(String email);
}
