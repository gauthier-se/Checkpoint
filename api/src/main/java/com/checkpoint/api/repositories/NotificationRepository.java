package com.checkpoint.api.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.entities.Notification;

/**
 * Repository for {@link Notification} entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Returns a paginated list of notifications for the given recipient,
     * ordered by creation date descending (newest first).
     *
     * @param recipientId the recipient's user ID
     * @param pageable    pagination parameters
     * @return a page of notifications
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    /**
     * Counts the number of unread notifications for the given recipient.
     *
     * @param recipientId the recipient's user ID
     * @return the unread notification count
     */
    long countByRecipientIdAndIsReadFalse(UUID recipientId);

    /**
     * Marks all unread notifications as read for the given recipient.
     *
     * @param recipientId the recipient's user ID
     * @return the number of updated notifications
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") UUID recipientId);
}
