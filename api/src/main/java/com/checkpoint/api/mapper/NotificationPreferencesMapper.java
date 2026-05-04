package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.notification.NotificationPreferencesDto;
import com.checkpoint.api.entities.NotificationPreferences;

/**
 * Mapper for converting NotificationPreferences entities to DTOs.
 */
public interface NotificationPreferencesMapper {

    /**
     * Converts a {@link NotificationPreferences} entity to a read DTO.
     *
     * @param preferences the entity (may be {@code null})
     * @return the DTO, or {@code null} if the entity was {@code null}
     */
    NotificationPreferencesDto toDto(NotificationPreferences preferences);
}
