package com.seyzeriat.desktop.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link NewsRequestPayload} serializes to the JSON shape
 * expected by the admin {@code POST /api/admin/news} and
 * {@code PUT /api/admin/news/{id}} endpoints.
 */
class NewsRequestPayloadTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("serializes title, description and picture as the API expects")
    void serialize_shouldProduceExpectedFields() throws Exception {
        // Given
        NewsRequestPayload payload = new NewsRequestPayload(
                "My title", "Body content", "https://example.test/cover.png");

        // When
        String json = objectMapper.writeValueAsString(payload);
        JsonNode node = objectMapper.readTree(json);

        // Then
        assertTrue(node.has("title"));
        assertTrue(node.has("description"));
        assertTrue(node.has("picture"));
        assertEquals("My title", node.get("title").asText());
        assertEquals("Body content", node.get("description").asText());
        assertEquals("https://example.test/cover.png", node.get("picture").asText());
    }

    @Test
    @DisplayName("a null picture is serialized as JSON null (the API treats it as absent)")
    void serialize_shouldKeepNullPicture() throws Exception {
        // Given
        NewsRequestPayload payload = new NewsRequestPayload("t", "d", null);

        // When
        String json = objectMapper.writeValueAsString(payload);
        JsonNode node = objectMapper.readTree(json);

        // Then
        assertTrue(node.has("picture"));
        assertTrue(node.get("picture").isNull());
    }
}
