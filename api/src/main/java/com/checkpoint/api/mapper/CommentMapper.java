package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.social.CommentResponseDto;
import com.checkpoint.api.entities.Comment;

/**
 * Mapper for converting Comment entities to DTOs.
 */
public interface CommentMapper {

    /**
     * Maps a Comment entity to a CommentResponseDto with default enrichment values
     * (0 likes, not liked, 0 replies).
     *
     * @param comment the comment entity
     * @return the comment response DTO
     */
    CommentResponseDto toDto(Comment comment);

    /**
     * Maps a Comment entity to a CommentResponseDto with enrichment data.
     *
     * @param comment      the comment entity
     * @param likesCount   the number of likes on this comment
     * @param hasLiked     whether the current viewer has liked this comment
     * @param repliesCount the number of replies on this comment
     * @return the comment response DTO
     */
    CommentResponseDto toDto(Comment comment, long likesCount, boolean hasLiked, long repliesCount);
}
