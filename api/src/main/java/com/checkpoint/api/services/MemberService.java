package com.checkpoint.api.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.social.MemberCardDto;

/**
 * Service for member discovery features (popular, top reviewers, suggestions, search).
 */
public interface MemberService {

    /**
     * Returns the most popular members ranked by follower count.
     *
     * @param pageable    pagination parameters
     * @param viewerEmail the authenticated viewer's email (nullable)
     * @return a list of member cards sorted by follower count descending
     */
    List<MemberCardDto> getPopularMembers(Pageable pageable, String viewerEmail);

    /**
     * Returns the top reviewers ranked by review count.
     *
     * @param pageable    pagination parameters
     * @param viewerEmail the authenticated viewer's email (nullable)
     * @return a list of member cards sorted by review count descending
     */
    List<MemberCardDto> getTopReviewers(Pageable pageable, String viewerEmail);

    /**
     * Returns personalized member suggestions based on shared games.
     * Requires an authenticated user.
     *
     * @param pageable    pagination parameters
     * @param viewerEmail the authenticated user's email
     * @return a list of suggested member cards sorted by shared game count descending
     */
    List<MemberCardDto> getSuggestedMembers(Pageable pageable, String viewerEmail);

    /**
     * Searches and browses members with optional pseudo filter.
     *
     * @param search      the search term for pseudo (nullable for browse all)
     * @param pageable    pagination parameters
     * @param viewerEmail the authenticated viewer's email (nullable)
     * @return a paginated list of member cards
     */
    Page<MemberCardDto> searchMembers(String search, Pageable pageable, String viewerEmail);
}
