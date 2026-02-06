package com.seyzeriat.desktop.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seyzeriat.desktop.dto.ExternalGameResult;
import com.seyzeriat.desktop.dto.ImportedGameResult;

/**
 * Service for communicating with the Checkpoint REST API.
 */
public class ApiService {

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Search for external games by keyword.
     *
     * @param query the search keyword
     * @param limit maximum number of results
     * @return list of matching external games
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    public List<ExternalGameResult> searchExternalGames(String query, int limit) throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "/api/admin/external-games/search?query=" + encodedQuery + "&limit=" + limit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Search failed with status " + response.statusCode() + ": " + response.body());
        }

        return objectMapper.readValue(response.body(), new TypeReference<List<ExternalGameResult>>() {});
    }

    /**
     * Import a game by its external ID.
     *
     * @param externalId the external (IGDB) game ID
     * @return the imported game details
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    public ImportedGameResult importGame(Long externalId) throws IOException, InterruptedException {
        String url = BASE_URL + "/api/admin/games/import/" + externalId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new IOException("Import failed with status " + response.statusCode() + ": " + response.body());
        }

        return objectMapper.readValue(response.body(), ImportedGameResult.class);
    }
}
