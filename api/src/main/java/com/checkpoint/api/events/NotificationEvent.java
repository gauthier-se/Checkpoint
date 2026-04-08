package com.checkpoint.api.events;

import java.util.UUID;

import com.checkpoint.api.enums.NotificationType;

/**
 * Event published when a notification should be created and delivered.
 * Consumed by the {@link com.checkpoint.api.listeners.NotificationListener}.
 */
public class NotificationEvent {

    private final UUID recipientId;
    private final UUID senderId;
    private final NotificationType type;
    private final UUID referenceId;
    private final String message;

    /**
     * Constructs a new NotificationEvent.
     *
     * @param recipientId the recipient user's ID
     * @param senderId    the sender user's ID (nullable for system notifications)
     * @param type        the notification type
     * @param referenceId the related entity ID (nullable)
     * @param message     the pre-formatted notification message
     */
    public NotificationEvent(UUID recipientId, UUID senderId, NotificationType type,
                             UUID referenceId, String message) {
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.type = type;
        this.referenceId = referenceId;
        this.message = message;
    }

    /**
     * Returns the recipient user's ID.
     *
     * @return the recipient ID
     */
    public UUID getRecipientId() {
        return recipientId;
    }

    /**
     * Returns the sender user's ID.
     *
     * @return the sender ID, or null for system notifications
     */
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * Returns the notification type.
     *
     * @return the notification type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Returns the related entity ID.
     *
     * @return the reference ID, or null
     */
    public UUID getReferenceId() {
        return referenceId;
    }

    /**
     * Returns the pre-formatted notification message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
