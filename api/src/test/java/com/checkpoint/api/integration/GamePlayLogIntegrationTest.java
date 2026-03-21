package com.checkpoint.api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.playlog.GamePlayLogRequestDto;
import com.checkpoint.api.entities.Platform;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.repositories.PlatformRepository;
import com.checkpoint.api.repositories.UserGamePlayRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.repositories.VideoGameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:playlogtest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.search.backend.type=lucene",
        "spring.jpa.properties.hibernate.search.backend.directory.type=local-heap"
})
class GamePlayLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserGamePlayRepository userGamePlayRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoGameRepository videoGameRepository;

    @Autowired
    private PlatformRepository platformRepository;

    private User testUser;
    private VideoGame testGame;
    private Platform testPlatform;

    @BeforeEach
    void setUp() {
        userGamePlayRepository.deleteAll();
        platformRepository.deleteAll();
        videoGameRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("player@example.com");
        testUser.setPassword("password");
        testUser.setPseudo("player1");
        testUser = userRepository.save(testUser);

        testGame = new VideoGame();
        testGame.setTitle("The Legend of Zelda");
        testGame = videoGameRepository.save(testGame);

        testPlatform = new Platform();
        testPlatform.setName("Nintendo Switch");
        testPlatform = platformRepository.save(testPlatform);
    }

    @Test
    @WithMockUser(username = "player@example.com")
    void shouldLogPlayAndRetrieveIt() throws Exception {
        // 1. Log a play session
        GamePlayLogRequestDto request = new GamePlayLogRequestDto(
                testGame.getId(), testPlatform.getId(), PlayStatus.COMPLETED,
                LocalDate.now().minusDays(5), LocalDate.now(), 3000, "owned", false, null
        );

        mockMvc.perform(post("/api/me/plays")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Legend of Zelda"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 2. Verify it's in the database
        assertThat(userGamePlayRepository.count()).isEqualTo(1);

        // 3. Retrieve through GET
        mockMvc.perform(get("/api/me/plays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("The Legend of Zelda"))
                .andExpect(jsonPath("$.metadata.totalElements").value(1));
    }
}
