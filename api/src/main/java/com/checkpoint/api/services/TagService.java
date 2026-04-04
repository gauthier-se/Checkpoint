package com.checkpoint.api.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.dto.tag.TagRequestDto;
import com.checkpoint.api.dto.tag.TagResponseDto;

/**
 * Service for managing user-scoped tags.
 */
public interface TagService {

    /**
     * Creates a new tag for the authenticated user.
     *
     * @param userEmail the authenticated user's email
     * @param request   the tag creation request
     * @return the created tag with play log count (0)
     */
    TagResponseDto createTag(String userEmail, TagRequestDto request);

    /**
     * Returns all tags belonging to the authenticated user with play log counts.
     *
     * @param userEmail the authenticated user's email
     * @return list of tags with counts
     */
    List<TagResponseDto> getUserTags(String userEmail);

    /**
     * Returns all tags belonging to a user by username (public access).
     *
     * @param username the target user's username
     * @return list of tags with counts
     */
    List<TagResponseDto> getPublicUserTags(String username);

    /**
     * Renames an existing tag.
     *
     * @param userEmail the authenticated user's email
     * @param tagId     the tag ID to rename
     * @param request   the rename request containing the new name
     * @return the updated tag
     */
    TagResponseDto updateTag(String userEmail, UUID tagId, TagRequestDto request);

    /**
     * Deletes a tag and removes all play log associations.
     *
     * @param userEmail the authenticated user's email
     * @param tagId     the tag ID to delete
     */
    void deleteTag(String userEmail, UUID tagId);

    /**
     * Returns paginated play logs associated with a specific tag (authenticated).
     *
     * @param userEmail the authenticated user's email
     * @param tagId     the tag ID
     * @param pageable  pagination parameters
     * @return page of play log responses
     */
    Page<GamePlayLogResponseDto> getPlayLogsByTag(String userEmail, UUID tagId, Pageable pageable);

    /**
     * Returns paginated play logs for a tag by name and username (public access).
     *
     * @param username the target user's username
     * @param tagName  the tag name
     * @param pageable pagination parameters
     * @return page of play log responses
     */
    Page<GamePlayLogResponseDto> getPublicPlayLogsByTag(String username, String tagName, Pageable pageable);
}
