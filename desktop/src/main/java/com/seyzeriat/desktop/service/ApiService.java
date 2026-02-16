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
 *
 * <p>Every outgoing request is intercepted to inject the JWT
 * {@code Authorization: Bearer {token}} header when a token is available.
 * If the server responds with 401 or 403, an {@link UnauthorizedException}
 * is thrown so the UI can redirect to the login screen.</p>
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
     * @throws IOException           if the request fails
     * @throws InterruptedException  if the request is interrupted
     * @throws UnauthorizedException if the token is expired or invalid
     */
    public List<ExternalGameResult> searchExternalGames(String query, int limit)
            throws IOException, InterruptedException, UnauthorizedException {

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "/api/admin/external-games/search?query=" + encodedQuery + "&limit=" + limit;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        addAuthHeader(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        checkUnauthorized(response);

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
     * @throws IOException           if the request fails
     * @throws InterruptedException  if the request is interrupted
     * @throws UnauthorizedException if the token is expired or invalid
     */
    public ImportedGameResult importGame(Long externalId)
            throws IOException, InterruptedException, UnauthorizedException {

        String url = BASE_URL + "/api/admin/games/import/" + externalId;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody());

        addAuthHeader(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        checkUnauthorized(response);

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new IOException("Import failed with status " + response.statusCode() + ": " + response.body());
        }

        return objectMapper.readValue(response.body(), ImportedGameResult.class);
    }

    // ─── Auth interceptor helpers ──────────────────────────────────────

    /**
     * Injects the JWT {@code Authorization: Bearer {token}} header into the
     * request builder if a token is available in {@link TokenManager}.
     */
    private void addAuthHeader(HttpRequest.Builder builder) {
        String token = TokenManager.getInstance().getToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
    }

    /**
     * Checks if the HTTP response indicates an authentication / authorization
     * failure and throws {@link UnauthorizedException} so the UI layer can
     * redirect to the login screen.
     */
    private void checkUnauthorized(HttpResponse<String> response) throws UnauthorizedException {
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            TokenManager.getInstance().clear();
            throw new UnauthorizedException(
                    "Session expirée ou accès refusé (HTTP " + response.statusCode() + "). Veuillez vous reconnecter.");
        }
    }

    /**
     * Thrown when the API returns 401 or 403, indicating the JWT is expired
     * or the user lacks permissions. The UI should catch this and redirect
     * to the login screen.
     */
    public static class UnauthorizedException extends Exception {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
