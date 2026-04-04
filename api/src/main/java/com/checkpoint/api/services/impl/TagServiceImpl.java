package com.checkpoint.api.services.impl;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.dto.tag.TagRequestDto;
import com.checkpoint.api.dto.tag.TagResponseDto;
import com.checkpoint.api.entities.Tag;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.exceptions.DuplicateTagException;
import com.checkpoint.api.exceptions.TagNotFoundException;
import com.checkpoint.api.exceptions.UserNotFoundException;
import com.checkpoint.api.mapper.GamePlayLogMapper;
import com.checkpoint.api.mapper.TagMapper;
import com.checkpoint.api.repositories.TagRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.TagService;

/**
 * Implementation of {@link TagService}.
 * Manages user-scoped tags and their association with play logs.
 */
@Service
@Transactional
public class TagServiceImpl implements TagService {

    private static final Logger log = LoggerFactory.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;
    private final GamePlayLogMapper gamePlayLogMapper;

    public TagServiceImpl(
            TagRepository tagRepository,
            UserRepository userRepository,
            TagMapper tagMapper,
            GamePlayLogMapper gamePlayLogMapper
    ) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.tagMapper = tagMapper;
        this.gamePlayLogMapper = gamePlayLogMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagResponseDto createTag(String userEmail, TagRequestDto request) {
        User user = getUserByEmail(userEmail);
        String normalizedName = request.name().trim().toLowerCase();

        if (tagRepository.existsByUserIdAndNameIgnoreCase(user.getId(), normalizedName)) {
            throw new DuplicateTagException("Tag with name '" + normalizedName + "' already exists");
        }

        Tag tag = new Tag(normalizedName, user);
        Tag savedTag = tagRepository.save(tag);

        log.info("Created tag '{}' for user {}", savedTag.getName(), userEmail);
        return tagMapper.toDto(savedTag, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TagResponseDto> getUserTags(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Tag> tags = tagRepository.findByUserIdOrderByNameAsc(user.getId());

        return tags.stream()
                .map(tag -> tagMapper.toDto(tag, tag.getPlayLogs().size()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TagResponseDto> getPublicUserTags(String username) {
        checkUserExists(username);
        List<Tag> tags = tagRepository.findByUserPseudoOrderByNameAsc(username);

        return tags.stream()
                .map(tag -> tagMapper.toDto(tag, tag.getPlayLogs().size()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagResponseDto updateTag(String userEmail, UUID tagId, TagRequestDto request) {
        User user = getUserByEmail(userEmail);
        Tag tag = getTagOwnedByUser(tagId, user.getId());
        String normalizedName = request.name().trim().toLowerCase();

        if (!tag.getName().equals(normalizedName)
                && tagRepository.existsByUserIdAndNameIgnoreCase(user.getId(), normalizedName)) {
            throw new DuplicateTagException("Tag with name '" + normalizedName + "' already exists");
        }

        tag.setName(normalizedName);
        Tag updatedTag = tagRepository.save(tag);

        log.info("Renamed tag {} to '{}' for user {}", tagId, updatedTag.getName(), userEmail);
        return tagMapper.toDto(updatedTag, updatedTag.getPlayLogs().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTag(String userEmail, UUID tagId) {
        User user = getUserByEmail(userEmail);
        Tag tag = getTagOwnedByUser(tagId, user.getId());

        tagRepository.delete(tag);
        log.info("Deleted tag {} ('{}') for user {}", tagId, tag.getName(), userEmail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<GamePlayLogResponseDto> getPlayLogsByTag(String userEmail, UUID tagId, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        getTagOwnedByUser(tagId, user.getId());

        Page<UserGamePlay> playLogs = tagRepository.findPlayLogsByTagId(tagId, pageable);
        return playLogs.map(gamePlayLogMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<GamePlayLogResponseDto> getPublicPlayLogsByTag(String username, String tagName, Pageable pageable) {
        checkUserExists(username);
        String normalizedName = tagName.trim().toLowerCase();

        tagRepository.findByNameAndUserPseudo(normalizedName, username)
                .orElseThrow(() -> new TagNotFoundException("Tag '" + tagName + "' not found for user " + username));

        Page<UserGamePlay> playLogs = tagRepository.findPlayLogsByTagNameAndUserPseudo(normalizedName, username, pageable);
        return playLogs.map(gamePlayLogMapper::toDto);
    }

    /**
     * Retrieves a user by email or throws if not found.
     *
     * @param email the user email
     * @return the user entity
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Retrieves a tag by ID and verifies it belongs to the given user.
     *
     * @param tagId  the tag ID
     * @param userId the user ID
     * @return the tag entity
     */
    private Tag getTagOwnedByUser(UUID tagId, UUID userId) {
        return tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with ID: " + tagId));
    }

    /**
     * Checks that a user with the given username exists.
     *
     * @param username the username
     */
    private void checkUserExists(String username) {
        userRepository.findByPseudo(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
}
