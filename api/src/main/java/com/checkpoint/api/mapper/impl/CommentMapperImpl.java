package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.social.CommentResponseDto;
import com.checkpoint.api.dto.social.CommentUserDto;
import com.checkpoint.api.entities.Comment;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.mapper.CommentMapper;

/**
 * Implementation of {@link CommentMapper}.
 */
@Component
public class CommentMapperImpl implements CommentMapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponseDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentUserDto userDto = null;
        if (comment.getUser() != null) {
            User user = comment.getUser();
            userDto = new CommentUserDto(
                    user.getId(),
                    user.getPseudo(),
                    user.getPicture()
            );
        }

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                userDto,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
