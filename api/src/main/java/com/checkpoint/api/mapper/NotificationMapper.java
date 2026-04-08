package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.notification.NotificationResponseDto;
import com.checkpoint.api.entities.Notification;

/**
 * Mapper for converting Notification entities to DTOs.
 */
public interface NotificationMapper {

    /**
     * Converts a Notification entity to a NotificationResponseDto.
     * Handles null sender gracefully for system notifications.
     *
     * @param notification the notification entity
     * @return the notification response DTO
     */
    NotificationResponseDto toResponseDto(Notification notification);
}
