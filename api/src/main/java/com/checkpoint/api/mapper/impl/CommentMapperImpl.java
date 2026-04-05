package com.checkpoint.api.mapper.impl;

import java.util.UUID;

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
        return toDto(comment, 0, false, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponseDto toDto(Comment comment, long likesCount, boolean hasLiked, long repliesCount) {
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

        UUID parentCommentId = comment.getParentComment() != null
                ? comment.getParentComment().getId()
                : null;

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                userDto,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                parentCommentId,
                repliesCount,
                likesCount,
                hasLiked
        );
    }
}
