package com.seyzeriat.desktop.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link NewsResult} deserializes the JSON shape returned by the
 * admin {@code /api/admin/news} endpoints. Acts as a contract test against the
 * API's {@code NewsResponseDto}.
 */
class NewsResultTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("deserializes a published news with author")
    void deserialize_shouldMapPublishedNewsWithAuthor() throws Exception {
        // Given
        String json = """
                {
                  "id": "00000000-0000-0000-0000-000000000001",
                  "title": "Patch 1.0",
                  "description": "# Hello\\n\\nworld",
                  "picture": "https://example.test/cover.png",
                  "publishedAt": "2026-04-30T08:11:32.306",
                  "createdAt": "2026-04-29T10:00:00.000",
                  "updatedAt": "2026-04-30T08:11:32.306",
                  "author": {
                    "id": "00000000-0000-0000-0000-000000000099",
                    "pseudo": "admin",
                    "picture": null
                  }
                }
                """;

        // When
        NewsResult result = objectMapper.readValue(json, NewsResult.class);

        // Then
        assertEquals("00000000-0000-0000-0000-000000000001", result.getId());
        assertEquals("Patch 1.0", result.getTitle());
        assertEquals("# Hello\n\nworld", result.getDescription());
        assertEquals("https://example.test/cover.png", result.getPicture());
        assertEquals("2026-04-30T08:11:32.306", result.getPublishedAt());
        assertEquals("2026-04-29T10:00:00.000", result.getCreatedAt());
        assertEquals("2026-04-30T08:11:32.306", result.getUpdatedAt());
        assertTrue(result.isPublished());

        assertNotNull(result.getAuthor());
        assertEquals("00000000-0000-0000-0000-000000000099", result.getAuthor().getId());
        assertEquals("admin", result.getAuthor().getPseudo());
        assertNull(result.getAuthor().getPicture());
    }

    @Test
    @DisplayName("a draft has no publishedAt and isPublished returns false")
    void deserialize_shouldHandleDraft() throws Exception {
        // Given
        // The API marks the field @JsonInclude(NON_NULL), so drafts simply omit publishedAt.
        String json = """
                {
                  "id": "00000000-0000-0000-0000-000000000002",
                  "title": "Brouillon",
                  "description": "todo",
                  "createdAt": "2026-04-29T10:00:00.000",
                  "updatedAt": "2026-04-29T10:00:00.000",
                  "author": {
                    "id": "00000000-0000-0000-0000-000000000099",
                    "pseudo": "admin"
                  }
                }
                """;

        // When
        NewsResult result = objectMapper.readValue(json, NewsResult.class);

        // Then
        assertNull(result.getPublishedAt());
        assertFalse(result.isPublished());
    }

    @Test
    @DisplayName("ignores unknown fields returned by the API")
    void deserialize_shouldIgnoreUnknownFields() throws Exception {
        // Given
        String json = """
                {
                  "id": "00000000-0000-0000-0000-000000000003",
                  "title": "X",
                  "newFieldFromApi": 42,
                  "author": null
                }
                """;

        // When / Then — no exception raised thanks to @JsonIgnoreProperties(ignoreUnknown = true)
        NewsResult result = objectMapper.readValue(json, NewsResult.class);
        assertEquals("X", result.getTitle());
    }
}
