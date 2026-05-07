package com.checkpoint.api.dto.collection;

import com.checkpoint.api.enums.Priority;

/**
 * Request body for updating the priority of a wishlist or backlog entry.
 *
 * @param priority the new priority, or {@code null} to clear it
 */
public record UpdatePriorityRequestDto(Priority priority) {
}
