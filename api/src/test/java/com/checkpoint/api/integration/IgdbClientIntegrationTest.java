package com.checkpoint.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

/**
 * Integration tests for IGDB API client.
 * These tests require valid IGDB_CLIENT_ID and IGDB_CLIENT_SECRET environment variables to be set.
 *
 * Uses H2 in-memory database to avoid requiring Docker.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:igdbtest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@EnabledIfEnvironmentVariable(named = "IGDB_CLIENT_ID", matches = ".+")
class IgdbClientIntegrationTest {

    @Autowired
    @Qualifier("igdbClient")
    private RestClient igdbClient;

    /**
     * Tests that the IGDB client can successfully call the /platforms endpoint
     * and receive a 200 OK response.
     * Note: IGDB uses POST requests with body for queries.
     */
    @Test
    void getPlatforms_shouldReturnOk() {
        var response = igdbClient.post()
                .uri("/platforms")
                .body("fields name; limit 10;")
                .retrieve()
                .toEntity(String.class);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("name"));
    }
}
