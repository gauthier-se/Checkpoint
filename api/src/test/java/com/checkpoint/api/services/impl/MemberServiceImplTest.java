package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.social.MemberCardDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.mapper.MemberMapper;
import com.checkpoint.api.mapper.impl.MemberMapperImpl;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;

/**
 * Unit tests for {@link MemberServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private MemberMapper memberMapper;
    private MemberServiceImpl memberService;

    private User userA;
    private User userB;
    private User viewer;

    @BeforeEach
    void setUp() {
        memberMapper = new MemberMapperImpl();
        memberService = new MemberServiceImpl(userRepository, reviewRepository, memberMapper);

        userA = new User();
        userA.setId(UUID.randomUUID());
        userA.setEmail("usera@example.com");
        userA.setPseudo("userA");
        userA.setLevel(5);

        userB = new User();
        userB.setId(UUID.randomUUID());
        userB.setEmail("userb@example.com");
        userB.setPseudo("userB");
        userB.setLevel(3);

        viewer = new User();
        viewer.setId(UUID.randomUUID());
        viewer.setEmail("viewer@example.com");
        viewer.setPseudo("viewer");
        viewer.setLevel(1);
    }

    @Nested
    @DisplayName("getPopularMembers")
    class GetPopularMembers {

        @Test
        @DisplayName("should return members sorted by follower count without auth")
        void getPopularMembers_shouldReturnMembersSortedByFollowerCount() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Object[]> page = createObjectArrayPage(new Object[]{userA, 100L}, new Object[]{userB, 50L});
            when(userRepository.findPopularMembers(pageable)).thenReturn(page);
            when(reviewRepository.countByUserPseudo("userA")).thenReturn(10L);
            when(reviewRepository.countByUserPseudo("userB")).thenReturn(5L);

            // When
            List<MemberCardDto> result = memberService.getPopularMembers(pageable, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).pseudo()).isEqualTo("userA");
            assertThat(result.get(0).followerCount()).isEqualTo(100L);
            assertThat(result.get(0).reviewCount()).isEqualTo(10L);
            assertThat(result.get(0).isFollowing()).isNull();
            assertThat(result.get(1).pseudo()).isEqualTo("userB");
            assertThat(result.get(1).followerCount()).isEqualTo(50L);
        }

        @Test
        @DisplayName("should populate isFollowing when authenticated")
        void getPopularMembers_shouldPopulateIsFollowingWhenAuthenticated() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Object[]> page = createObjectArrayPage(new Object[]{userA, 100L});
            when(userRepository.findPopularMembers(pageable)).thenReturn(page);
            when(reviewRepository.countByUserPseudo("userA")).thenReturn(10L);
            when(userRepository.findByEmail("viewer@example.com")).thenReturn(Optional.of(viewer));
            when(userRepository.findFollowingIdsByUserId(viewer.getId())).thenReturn(List.of(userA.getId()));

            // When
            List<MemberCardDto> result = memberService.getPopularMembers(pageable, "viewer@example.com");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isFollowing()).isTrue();
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void getPopularMembers_shouldReturnEmptyList() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Object[]> page = createObjectArrayPage();
            when(userRepository.findPopularMembers(pageable)).thenReturn(page);

            // When
            List<MemberCardDto> result = memberService.getPopularMembers(pageable, null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTopReviewers")
    class GetTopReviewers {

        @Test
        @DisplayName("should return members sorted by review count without auth")
        void getTopReviewers_shouldReturnMembersSortedByReviewCount() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Object[]> page = createObjectArrayPage(new Object[]{userA, 50L}, new Object[]{userB, 20L});
            when(userRepository.findTopReviewers(pageable)).thenReturn(page);
            when(userRepository.countFollowersByUserId(userA.getId())).thenReturn(10L);
            when(userRepository.countFollowersByUserId(userB.getId())).thenReturn(5L);

            // When
            List<MemberCardDto> result = memberService.getTopReviewers(pageable, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).pseudo()).isEqualTo("userA");
            assertThat(result.get(0).reviewCount()).isEqualTo(50L);
            assertThat(result.get(0).followerCount()).isEqualTo(10L);
            assertThat(result.get(0).isFollowing()).isNull();
        }

        @Test
        @DisplayName("should populate isFollowing when authenticated")
        void getTopReviewers_shouldPopulateIsFollowingWhenAuthenticated() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Object[]> page = createObjectArrayPage(new Object[]{userA, 50L});
            when(userRepository.findTopReviewers(pageable)).thenReturn(page);
            when(userRepository.countFollowersByUserId(userA.getId())).thenReturn(10L);
            when(userRepository.findByEmail("viewer@example.com")).thenReturn(Optional.of(viewer));
            when(userRepository.findFollowingIdsByUserId(viewer.getId())).thenReturn(List.of());

            // When
            List<MemberCardDto> result = memberService.getTopReviewers(pageable, "viewer@example.com");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isFollowing()).isFalse();
        }
    }

    @Nested
    @DisplayName("getSuggestedMembers")
    class GetSuggestedMembers {

        @Test
        @DisplayName("should return suggested members based on shared games")
        void getSuggestedMembers_shouldReturnSuggestions() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findByEmail("viewer@example.com")).thenReturn(Optional.of(viewer));
            when(userRepository.findFollowingIdsByUserId(viewer.getId())).thenReturn(List.of());

            Page<Object[]> page = createObjectArrayPage(new Object[]{userA, 5L});
            when(userRepository.findSuggestedMembers(viewer.getId(), pageable)).thenReturn(page);
            when(userRepository.countFollowersByUserId(userA.getId())).thenReturn(20L);
            when(reviewRepository.countByUserPseudo("userA")).thenReturn(8L);

            // When
            List<MemberCardDto> result = memberService.getSuggestedMembers(pageable, "viewer@example.com");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).pseudo()).isEqualTo("userA");
            assertThat(result.get(0).followerCount()).isEqualTo(20L);
            assertThat(result.get(0).reviewCount()).isEqualTo(8L);
            assertThat(result.get(0).isFollowing()).isFalse();
        }

        @Test
        @DisplayName("should throw when viewer not found")
        void getSuggestedMembers_shouldThrowWhenViewerNotFound() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> memberService.getSuggestedMembers(pageable, "unknown@example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Authenticated user not found");
        }

        @Test
        @DisplayName("should return empty list when no suggestions")
        void getSuggestedMembers_shouldReturnEmptyWhenNoSuggestions() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findByEmail("viewer@example.com")).thenReturn(Optional.of(viewer));
            when(userRepository.findFollowingIdsByUserId(viewer.getId())).thenReturn(List.of());
            when(userRepository.findSuggestedMembers(viewer.getId(), pageable))
                    .thenReturn(createObjectArrayPage());

            // When
            List<MemberCardDto> result = memberService.getSuggestedMembers(pageable, "viewer@example.com");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchMembers")
    class SearchMembers {

        @Test
        @DisplayName("should return all members when no search term")
        void searchMembers_shouldReturnAllMembersWhenNoSearch() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> usersPage = new PageImpl<>(List.of(userA, userB));
            when(userRepository.findAll(pageable)).thenReturn(usersPage);
            when(userRepository.countFollowersByUserId(userA.getId())).thenReturn(10L);
            when(userRepository.countFollowersByUserId(userB.getId())).thenReturn(5L);
            when(reviewRepository.countByUserPseudo("userA")).thenReturn(3L);
            when(reviewRepository.countByUserPseudo("userB")).thenReturn(1L);

            // When
            Page<MemberCardDto> result = memberService.searchMembers(null, pageable, null);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).pseudo()).isEqualTo("userA");
            assertThat(result.getContent().get(0).isFollowing()).isNull();
        }

        @Test
        @DisplayName("should filter members by search term")
        void searchMembers_shouldFilterBySearchTerm() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> usersPage = new PageImpl<>(List.of(userA));
            when(userRepository.findByPseudoContainingIgnoreCase("userA", pageable)).thenReturn(usersPage);
            when(userRepository.countFollowersByUserId(userA.getId())).thenReturn(10L);
            when(reviewRepository.countByUserPseudo("userA")).thenReturn(3L);

            // When
            Page<MemberCardDto> result = memberService.searchMembers("userA", pageable, null);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).pseudo()).isEqualTo("userA");
        }

        @Test
        @DisplayName("should ignore blank search term")
        void searchMembers_shouldIgnoreBlankSearch() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> usersPage = new PageImpl<>(List.of(userA));
            when(userRepository.findAll(pageable)).thenReturn(usersPage);
            when(userRepository.countFollowersByUserId(userA.getId())).thenReturn(10L);
            when(reviewRepository.countByUserPseudo("userA")).thenReturn(3L);

            // When
            Page<MemberCardDto> result = memberService.searchMembers("   ", pageable, null);

            // Then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should populate isFollowing when authenticated")
        void searchMembers_shouldPopulateIsFollowingWhenAuthenticated() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> usersPage = new PageImpl<>(List.of(userA));
            when(userRepository.findAll(pageable)).thenReturn(usersPage);
            when(userRepository.countFollowersByUserId(userA.getId())).thenReturn(10L);
            when(reviewRepository.countByUserPseudo("userA")).thenReturn(3L);
            when(userRepository.findByEmail("viewer@example.com")).thenReturn(Optional.of(viewer));
            when(userRepository.findFollowingIdsByUserId(viewer.getId())).thenReturn(List.of(userA.getId()));

            // When
            Page<MemberCardDto> result = memberService.searchMembers(null, pageable, "viewer@example.com");

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).isFollowing()).isTrue();
        }
    }

    /**
     * Helper to create a Page of Object[] without type inference issues.
     */
    private Page<Object[]> createObjectArrayPage(Object[]... rows) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] row : rows) {
            list.add(row);
        }
        return new PageImpl<>(list);
    }
}
