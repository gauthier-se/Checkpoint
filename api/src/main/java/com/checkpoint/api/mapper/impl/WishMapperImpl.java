package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.collection.WishResponseDto;
import com.checkpoint.api.entities.Wish;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.mapper.WishMapper;

/**
 * Implementation of {@link WishMapper}.
 */
@Component
public class WishMapperImpl implements WishMapper {

    @Override
    public WishResponseDto toResponseDto(Wish wish) {
        VideoGame videoGame = wish.getVideoGame();

        return new WishResponseDto(
                wish.getId(),
                videoGame.getId(),
                videoGame.getTitle(),
                videoGame.getCoverUrl(),
                videoGame.getReleaseDate(),
                wish.getCreatedAt()
        );
    }
}
