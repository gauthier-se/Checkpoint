package com.checkpoint.api.mapper;

import java.util.List;

import com.checkpoint.api.dto.list.GameListCardDto;
import com.checkpoint.api.dto.list.GameListDetailDto;
import com.checkpoint.api.dto.list.GameListEntryDto;
import com.checkpoint.api.entities.GameList;
import com.checkpoint.api.entities.GameListEntry;

/**
 * Mapper for converting GameList entities to DTOs.
 */
public interface GameListMapper {

    /**
     * Converts a GameList entity to a card DTO for browse/listing views.
     *
     * @param gameList      the game list entity
     * @param likesCount    the number of likes for this list
     * @param commentsCount the number of comments for this list
     * @param coverUrls     cover URLs of the first games in the list (max 4)
     * @return the card DTO
     */
    GameListCardDto toCardDto(GameList gameList, long likesCount, long commentsCount, List<String> coverUrls);

    /**
     * Converts a GameList entity to a detail DTO.
     *
     * @param gameList      the game list entity
     * @param entries       the ordered list entries with fetched video games
     * @param likesCount    the number of likes for this list
     * @param commentsCount the number of comments for this list
     * @param isOwner       whether the viewer owns this list
     * @param hasLiked      whether the viewer has liked this list
     * @return the detail DTO
     */
    GameListDetailDto toDetailDto(GameList gameList, List<GameListEntry> entries,
                                  long likesCount, long commentsCount, boolean isOwner, boolean hasLiked);

    /**
     * Converts a GameListEntry entity to an entry DTO.
     *
     * @param entry the game list entry entity
     * @return the entry DTO
     */
    GameListEntryDto toEntryDto(GameListEntry entry);
}
