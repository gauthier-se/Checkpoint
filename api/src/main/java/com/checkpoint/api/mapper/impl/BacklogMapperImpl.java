package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.collection.BacklogResponseDto;
import com.checkpoint.api.entities.Backlog;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.mapper.BacklogMapper;

/**
 * Implementation of {@link BacklogMapper}.
 */
@Component
public class BacklogMapperImpl implements BacklogMapper {

    @Override
    public BacklogResponseDto toResponseDto(Backlog backlog) {
        VideoGame videoGame = backlog.getVideoGame();

        return new BacklogResponseDto(
                backlog.getId(),
                videoGame.getId(),
                videoGame.getTitle(),
                videoGame.getCoverUrl(),
                videoGame.getReleaseDate(),
                backlog.getCreatedAt()
        );
    }
}
