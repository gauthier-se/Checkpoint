package com.checkpoint.api.dto.notification;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for the bulk mark-as-read request.
 *
 * @param ids the set of notification IDs to mark as read
 */
public record BulkMarkAsReadDto(
        @NotEmpty Set<UUID> ids
) {}
