package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.social.CommentResponseDto;
import com.checkpoint.api.entities.Comment;

/**
 * Mapper for converting Comment entities to DTOs.
 */
public interface CommentMapper {

    /**
     * Maps a Comment entity to a CommentResponseDto.
     *
     * @param comment the comment entity
     * @return the comment response DTO
     */
    CommentResponseDto toDto(Comment comment);
}
