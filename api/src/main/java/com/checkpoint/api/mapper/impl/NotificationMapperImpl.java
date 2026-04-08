package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.notification.NotificationResponseDto;
import com.checkpoint.api.entities.Notification;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.mapper.NotificationMapper;

/**
 * Implementation of {@link NotificationMapper}.
 */
@Component
public class NotificationMapperImpl implements NotificationMapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationResponseDto toResponseDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        User sender = notification.getSender();
        String senderPseudo = sender != null ? sender.getPseudo() : null;
        String senderPicture = sender != null ? sender.getPicture() : null;

        return new NotificationResponseDto(
                notification.getId(),
                senderPseudo,
                senderPicture,
                notification.getType(),
                notification.getReferenceId(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
