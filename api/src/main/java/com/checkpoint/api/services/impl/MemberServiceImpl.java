package com.checkpoint.api.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.social.MemberCardDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.mapper.MemberMapper;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.MemberService;

/**
 * Implementation of {@link MemberService}.
 * Provides member discovery features with optimized queries.
 */
@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MemberMapper memberMapper;

    public MemberServiceImpl(UserRepository userRepository,
                             ReviewRepository reviewRepository,
                             MemberMapper memberMapper) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.memberMapper = memberMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MemberCardDto> getPopularMembers(Pageable pageable, String viewerEmail) {
        log.info("Fetching popular members (size={})", pageable.getPageSize());

        Set<UUID> followingIds = getFollowingIds(viewerEmail);
        Page<Object[]> page = userRepository.findPopularMembers(pageable);

        return page.getContent().stream()
                .map(row -> {
                    User user = (User) row[0];
                    Long followerCount = (Long) row[1];
                    long reviewCount = reviewRepository.countByUserPseudo(user.getPseudo());
                    Boolean isFollowing = resolveIsFollowing(user.getId(), followingIds, viewerEmail);
                    return memberMapper.toMemberCardDto(user, followerCount, reviewCount, isFollowing);
                })
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MemberCardDto> getTopReviewers(Pageable pageable, String viewerEmail) {
        log.info("Fetching top reviewers (size={})", pageable.getPageSize());

        Set<UUID> followingIds = getFollowingIds(viewerEmail);
        Page<Object[]> page = userRepository.findTopReviewers(pageable);

        return page.getContent().stream()
                .map(row -> {
                    User user = (User) row[0];
                    Long reviewCount = (Long) row[1];
                    long followerCount = userRepository.countFollowersByUserId(user.getId());
                    Boolean isFollowing = resolveIsFollowing(user.getId(), followingIds, viewerEmail);
                    return memberMapper.toMemberCardDto(user, followerCount, reviewCount, isFollowing);
                })
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MemberCardDto> getSuggestedMembers(Pageable pageable, String viewerEmail) {
        log.info("Fetching suggested members for user: {}", viewerEmail);

        User viewer = userRepository.findByEmail(viewerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Set<UUID> followingIds = new HashSet<>(userRepository.findFollowingIdsByUserId(viewer.getId()));
        Page<Object[]> page = userRepository.findSuggestedMembers(viewer.getId(), pageable);

        return page.getContent().stream()
                .map(row -> {
                    User user = (User) row[0];
                    long followerCount = userRepository.countFollowersByUserId(user.getId());
                    long reviewCount = reviewRepository.countByUserPseudo(user.getPseudo());
                    Boolean isFollowing = resolveIsFollowing(user.getId(), followingIds, viewerEmail);
                    return memberMapper.toMemberCardDto(user, followerCount, reviewCount, isFollowing);
                })
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<MemberCardDto> searchMembers(String search, Pageable pageable, String viewerEmail) {
        log.info("Searching members (search={}, page={}, size={})",
                search, pageable.getPageNumber(), pageable.getPageSize());

        Set<UUID> followingIds = getFollowingIds(viewerEmail);

        Page<User> usersPage;
        if (search != null && !search.isBlank()) {
            usersPage = userRepository.findByPseudoContainingIgnoreCase(search.trim(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        return usersPage.map(user -> {
            long followerCount = userRepository.countFollowersByUserId(user.getId());
            long reviewCount = reviewRepository.countByUserPseudo(user.getPseudo());
            Boolean isFollowing = resolveIsFollowing(user.getId(), followingIds, viewerEmail);
            return memberMapper.toMemberCardDto(user, followerCount, reviewCount, isFollowing);
        });
    }

    /**
     * Loads the set of user IDs that the viewer follows.
     * Returns an empty set if the viewer is not authenticated.
     *
     * @param viewerEmail the viewer's email (nullable)
     * @return set of followed user IDs
     */
    private Set<UUID> getFollowingIds(String viewerEmail) {
        if (viewerEmail == null) {
            return Set.of();
        }
        return userRepository.findByEmail(viewerEmail)
                .map(viewer -> new HashSet<>(userRepository.findFollowingIdsByUserId(viewer.getId())))
                .orElse(new HashSet<>());
    }

    /**
     * Resolves whether the viewer follows the given user.
     * Returns null if the viewer is not authenticated.
     *
     * @param userId       the target user's ID
     * @param followingIds the set of user IDs the viewer follows
     * @param viewerEmail  the viewer's email (nullable)
     * @return true/false if authenticated, null otherwise
     */
    private Boolean resolveIsFollowing(UUID userId, Set<UUID> followingIds, String viewerEmail) {
        if (viewerEmail == null) {
            return null;
        }
        return followingIds.contains(userId);
    }
}
