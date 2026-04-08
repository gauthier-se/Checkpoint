package com.checkpoint.api.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.dto.notification.NotificationResponseDto;
import com.checkpoint.api.dto.notification.UnreadCountDto;
import com.checkpoint.api.services.NotificationService;

/**
 * REST controller for managing the authenticated user's notifications.
 *
 * <p>All endpoints require authentication and operate on the current user's
 * notifications only.</p>
 */
@RestController
@RequestMapping("/api/me/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Returns a paginated list of notifications for the authenticated user.
     *
     * @param userDetails the authenticated user principal
     * @param page        the page number (0-based, default 0)
     * @param size        the page size (default 20, max 100)
     * @return paginated list of notification DTOs
     */
    @GetMapping
    public ResponseEntity<PagedResponseDto<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size) {

        log.info("GET /api/me/notifications - user: {}, page: {}, size: {}", userDetails.getUsername(), page, size);

        int validatedSize = Math.min(Math.max(1, size), MAX_SIZE);
        int validatedPage = Math.max(0, page);

        PagedResponseDto<NotificationResponseDto> response = notificationService.getNotifications(
                userDetails.getUsername(), validatedPage, validatedSize);

        return ResponseEntity.ok(response);
    }

    /**
     * Returns the number of unread notifications for the authenticated user.
     *
     * @param userDetails the authenticated user principal
     * @return the unread notification count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountDto> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/me/notifications/unread-count - user: {}", userDetails.getUsername());

        UnreadCountDto response = notificationService.getUnreadCount(userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

    /**
     * Marks a single notification as read.
     * The notification must belong to the authenticated user.
     *
     * @param userDetails    the authenticated user principal
     * @param notificationId the notification ID
     * @return 204 No Content
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID notificationId) {

        log.info("PUT /api/me/notifications/{}/read - user: {}", notificationId, userDetails.getUsername());

        notificationService.markAsRead(notificationId, userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }

    /**
     * Marks all notifications as read for the authenticated user.
     *
     * @param userDetails the authenticated user principal
     * @return 204 No Content
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("PUT /api/me/notifications/read-all - user: {}", userDetails.getUsername());

        notificationService.markAllAsRead(userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }
}
