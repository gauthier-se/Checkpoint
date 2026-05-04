package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.dto.notification.NotificationPreferencesDto;
import com.checkpoint.api.dto.notification.UpdateNotificationPreferencesDto;
import com.checkpoint.api.enums.NotificationType;

/**
 * Service for managing per-user notification preferences.
 *
 * <p>Users have a 1:1 row in {@code notification_preferences}. The row is lazily
 * created on first access via {@link #getOrCreate(String)} or
 * {@link #update(String, UpdateNotificationPreferencesDto)}; users without a row
 * are treated as fully opted-in.</p>
 */
public interface NotificationPreferencesService {

    /**
     * Returns the preferences for the given user, creating the row with all
     * preferences enabled if it does not yet exist.
     *
     * @param userEmail the authenticated user's email
     * @return the preferences DTO
     */
    NotificationPreferencesDto getOrCreate(String userEmail);

    /**
     * Applies a partial update to the user's preferences. Fields set to
     * {@code null} in {@code dto} are left unchanged. If no row exists, a fresh
     * one is created with default values before applying the update.
     *
     * @param userEmail the authenticated user's email
     * @param dto       the partial update payload
     * @return the updated preferences DTO
     */
    NotificationPreferencesDto update(String userEmail, UpdateNotificationPreferencesDto dto);

    /**
     * Returns whether the recipient has notifications of the given type enabled.
     * Users with no preferences row default to {@code true} (fully opted-in) so
     * missing rows can never silence notifications by accident.
     *
     * @param recipientId the recipient user's ID
     * @param type        the notification type to check
     * @return {@code true} if the type is enabled or no row exists yet
     */
    boolean isEnabled(UUID recipientId, NotificationType type);
}
