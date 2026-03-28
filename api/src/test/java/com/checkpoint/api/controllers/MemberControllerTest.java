package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.social.MemberCardDto;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.MemberService;

/**
 * Unit tests for {@link MemberController}.
 */
@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    private MemberCardDto createMemberCard(String pseudo, Long followerCount, Long reviewCount, Boolean isFollowing) {
        return new MemberCardDto(UUID.randomUUID(), pseudo, null, 1, followerCount, reviewCount, isFollowing);
    }

    @Nested
    @DisplayName("GET /api/members/popular")
    class GetPopularMembers {

        @Test
        @DisplayName("should return popular members sorted by follower count")
        void getPopularMembers_shouldReturnMembers() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("popular1", 100L, 5L, null),
                    createMemberCard("popular2", 80L, 3L, null)
            );
            when(memberService.getPopularMembers(any(Pageable.class), eq(null)))
                    .thenReturn(members);

            // When / Then
            mockMvc.perform(get("/api/members/popular"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].pseudo").value("popular1"))
                    .andExpect(jsonPath("$[0].followerCount").value(100))
                    .andExpect(jsonPath("$[1].pseudo").value("popular2"));
        }

        @Test
        @DisplayName("should accept custom size parameter")
        void getPopularMembers_shouldAcceptCustomSize() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("popular1", 100L, 5L, null)
            );
            when(memberService.getPopularMembers(any(Pageable.class), eq(null)))
                    .thenReturn(members);

            // When / Then
            mockMvc.perform(get("/api/members/popular").param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("should include isFollowing when authenticated")
        @WithMockUser(username = "viewer@example.com")
        void getPopularMembers_shouldIncludeIsFollowingWhenAuthenticated() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("popular1", 100L, 5L, true)
            );
            when(memberService.getPopularMembers(any(Pageable.class), eq("viewer@example.com")))
                    .thenReturn(members);

            // When / Then
            mockMvc.perform(get("/api/members/popular"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].isFollowing").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/members/top-reviewers")
    class GetTopReviewers {

        @Test
        @DisplayName("should return top reviewers sorted by review count")
        void getTopReviewers_shouldReturnMembers() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("reviewer1", 10L, 50L, null),
                    createMemberCard("reviewer2", 5L, 30L, null)
            );
            when(memberService.getTopReviewers(any(Pageable.class), eq(null)))
                    .thenReturn(members);

            // When / Then
            mockMvc.perform(get("/api/members/top-reviewers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].pseudo").value("reviewer1"))
                    .andExpect(jsonPath("$[0].reviewCount").value(50));
        }

        @Test
        @DisplayName("should include isFollowing when authenticated")
        @WithMockUser(username = "viewer@example.com")
        void getTopReviewers_shouldIncludeIsFollowingWhenAuthenticated() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("reviewer1", 10L, 50L, false)
            );
            when(memberService.getTopReviewers(any(Pageable.class), eq("viewer@example.com")))
                    .thenReturn(members);

            // When / Then
            mockMvc.perform(get("/api/members/top-reviewers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].isFollowing").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/members/suggested")
    class GetSuggestedMembers {

        @Test
        @DisplayName("should return suggested members when authenticated")
        @WithMockUser(username = "viewer@example.com")
        void getSuggestedMembers_shouldReturnSuggestions() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("suggestion1", 20L, 10L, false)
            );
            when(memberService.getSuggestedMembers(any(Pageable.class), eq("viewer@example.com")))
                    .thenReturn(members);

            // When / Then
            mockMvc.perform(get("/api/members/suggested"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].pseudo").value("suggestion1"))
                    .andExpect(jsonPath("$[0].isFollowing").value(false));
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void getSuggestedMembers_shouldReturn401WhenNotAuthenticated() throws Exception {
            // When / Then
            mockMvc.perform(get("/api/members/suggested"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/members")
    class SearchMembers {

        @Test
        @DisplayName("should return paginated members without search filter")
        void searchMembers_shouldReturnPaginatedMembers() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("alice", 10L, 5L, null),
                    createMemberCard("bob", 20L, 8L, null)
            );
            Page<MemberCardDto> page = new PageImpl<>(members);
            when(memberService.searchMembers(eq(null), any(Pageable.class), eq(null)))
                    .thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/members"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].pseudo").value("alice"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(2));
        }

        @Test
        @DisplayName("should filter members by search term")
        void searchMembers_shouldFilterBySearchTerm() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("alice", 10L, 5L, null)
            );
            Page<MemberCardDto> page = new PageImpl<>(members);
            when(memberService.searchMembers(eq("ali"), any(Pageable.class), eq(null)))
                    .thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/members").param("search", "ali"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].pseudo").value("alice"));
        }

        @Test
        @DisplayName("should accept pagination parameters")
        void searchMembers_shouldAcceptPaginationParams() throws Exception {
            // Given
            List<MemberCardDto> members = List.of();
            Page<MemberCardDto> page = new PageImpl<>(members);
            when(memberService.searchMembers(eq(null), any(Pageable.class), eq(null)))
                    .thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/members")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("should include isFollowing when authenticated")
        @WithMockUser(username = "viewer@example.com")
        void searchMembers_shouldIncludeIsFollowingWhenAuthenticated() throws Exception {
            // Given
            List<MemberCardDto> members = List.of(
                    createMemberCard("alice", 10L, 5L, true)
            );
            Page<MemberCardDto> page = new PageImpl<>(members);
            when(memberService.searchMembers(eq(null), any(Pageable.class), eq("viewer@example.com")))
                    .thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/members"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].isFollowing").value(true));
        }
    }
}
