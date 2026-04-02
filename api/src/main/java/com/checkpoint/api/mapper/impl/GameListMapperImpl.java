package com.checkpoint.api.mapper.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.list.GameListCardDto;
import com.checkpoint.api.dto.list.GameListDetailDto;
import com.checkpoint.api.dto.list.GameListEntryDto;
import com.checkpoint.api.entities.GameList;
import com.checkpoint.api.entities.GameListEntry;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.mapper.GameListMapper;

/**
 * Implementation of {@link GameListMapper}.
 */
@Component
public class GameListMapperImpl implements GameListMapper {

    @Override
    public GameListCardDto toCardDto(GameList gameList, long likesCount, long commentsCount, List<String> coverUrls) {
        User author = gameList.getUser();

        return new GameListCardDto(
                gameList.getId(),
                gameList.getTitle(),
                gameList.getDescription(),
                gameList.getIsPrivate(),
                gameList.getVideoGamesCount(),
                likesCount,
                commentsCount,
                author.getPseudo(),
                author.getPicture(),
                coverUrls,
                gameList.getCreatedAt()
        );
    }

    @Override
    public GameListDetailDto toDetailDto(GameList gameList, List<GameListEntry> entries,
                                         long likesCount, long commentsCount, boolean isOwner, boolean hasLiked) {
        User author = gameList.getUser();
        List<GameListEntryDto> entryDtos = entries.stream()
                .map(this::toEntryDto)
                .toList();

        return new GameListDetailDto(
                gameList.getId(),
                gameList.getTitle(),
                gameList.getDescription(),
                gameList.getIsPrivate(),
                gameList.getVideoGamesCount(),
                likesCount,
                commentsCount,
                author.getPseudo(),
                author.getPicture(),
                entryDtos,
                isOwner,
                hasLiked,
                gameList.getCreatedAt(),
                gameList.getUpdatedAt()
        );
    }

    @Override
    public GameListEntryDto toEntryDto(GameListEntry entry) {
        VideoGame videoGame = entry.getVideoGame();

        return new GameListEntryDto(
                videoGame.getId(),
                videoGame.getTitle(),
                videoGame.getCoverUrl(),
                videoGame.getReleaseDate(),
                entry.getPosition(),
                entry.getAddedAt()
        );
    }
}
