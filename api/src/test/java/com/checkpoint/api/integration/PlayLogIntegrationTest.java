package com.checkpoint.api.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.entities.Like;
import com.checkpoint.api.entities.Platform;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.repositories.LikeRepository;
import com.checkpoint.api.repositories.PlatformRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserGamePlayRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;

/**
 * Integration tests for the public {@code GET /api/plays/{id}} endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:playlogdetailtest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.search.backend.type=lucene",
        "spring.jpa.properties.hibernate.search.backend.directory.type=local-heap"
})
class PlayLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VideoGameRepository videoGameRepository;
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private UserGamePlayRepository userGamePlayRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private LikeRepository likeRepository;

    private User author;
    private VideoGame game;
    private Platform platform;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
        reviewRepository.deleteAll();
        userGamePlayRepository.deleteAll();
        platformRepository.deleteAll();
        videoGameRepository.deleteAll();
        userRepository.deleteAll();

        author = new User();
        author.setEmail("author@example.com");
        author.setPassword("password");
        author.setPseudo("geralt");
        author = userRepository.save(author);

        game = new VideoGame();
        game.setTitle("The Witcher 3");
        game.setReleaseDate(LocalDate.of(2015, 5, 19));
        game = videoGameRepository.save(game);

        platform = new Platform();
        platform.setName("PC");
        platform = platformRepository.save(platform);
    }

    private UserGamePlay savePlay(boolean withReview) {
        UserGamePlay play = new UserGamePlay(author, game, platform, PlayStatus.COMPLETED);
        play.setIsReplay(false);
        play.setTimePlayed(3000);
        play.setStartDate(LocalDate.now().minusDays(10));
        play.setEndDate(LocalDate.now());
        play.setOwnership("owned");
        play.setScore(9);
        play.setTags(Set.of());
        play = userGamePlayRepository.save(play);

        if (withReview) {
            Review review = new Review("Great game!", false, author, game, play);
            review = reviewRepository.save(review);
            play.setReview(review);
            play = userGamePlayRepository.save(play);
        }
        return play;
    }

    @Test
    void shouldReturnPlayLogDetailForPublicProfile() throws Exception {
        UserGamePlay play = savePlay(true);

        mockMvc.perform(get("/api/plays/{id}", play.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(play.getId().toString()))
                .andExpect(jsonPath("$.title").value("The Witcher 3"))
                .andExpect(jsonPath("$.username").value("geralt"))
                .andExpect(jsonPath("$.score").value(9))
                .andExpect(jsonPath("$.review.content").value("Great game!"))
                .andExpect(jsonPath("$.review.likeCount").value(0))
                .andExpect(jsonPath("$.review.commentCount").value(0));
    }

    @Test
    void shouldOmitReviewBlockWhenAbsent() throws Exception {
        UserGamePlay play = savePlay(false);

        mockMvc.perform(get("/api/plays/{id}", play.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review").doesNotExist());
    }

    @Test
    void shouldReturn404WhenPlayLogDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/plays/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenAuthorIsPrivateAndViewerIsNotOwner() throws Exception {
        author.setIsPrivate(true);
        userRepository.save(author);
        UserGamePlay play = savePlay(false);

        mockMvc.perform(get("/api/plays/{id}", play.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "author@example.com")
    void shouldReturn200WhenOwnerReadsTheirPrivatePlay() throws Exception {
        author.setIsPrivate(true);
        userRepository.save(author);
        UserGamePlay play = savePlay(false);

        mockMvc.perform(get("/api/plays/{id}", play.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isOwner").value(true));
    }

    @Test
    @WithMockUser(username = "author@example.com")
    void shouldReflectViewerGameLikeWhenAuthenticated() throws Exception {
        UserGamePlay play = savePlay(false);
        likeRepository.save(Like.forVideoGame(author, game));

        mockMvc.perform(get("/api/plays/{id}", play.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLikedByViewer").value(true));
    }
}
