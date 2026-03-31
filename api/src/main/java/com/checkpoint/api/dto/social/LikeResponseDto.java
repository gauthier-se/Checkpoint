package com.checkpoint.api.dto.social;

/**
 * DTO for the like/unlike toggle response.
 *
 * @param liked      whether the authenticated user now likes the target
 * @param likesCount the updated total number of likes on the target
 */
public record LikeResponseDto(
        boolean liked,
        long likesCount
) {}
