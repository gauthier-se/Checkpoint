package com.seyzeriat.desktop.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seyzeriat.desktop.dto.LoginResponseDto;

/**
 * Service responsible for authenticating against the Checkpoint API.
 *
 * <p>Sends credentials to {@code POST /api/auth/token} (the dedicated Desktop
 * JWT endpoint) and stores the received token pair in {@link TokenManager}.</p>
 */
public class AuthService {

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Authenticate with email and password.
     * Stores both the access token and refresh token in {@link TokenManager}.
     *
     * @param email    the user's email
     * @param password the user's password
     * @throws AuthenticationException if the credentials are invalid or the server is unreachable
     */
    public void login(String email, String password) throws AuthenticationException {
        String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/token"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                LoginResponseDto dto = objectMapper.readValue(response.body(), LoginResponseDto.class);
                if (dto.getAccessToken() == null || dto.getAccessToken().isBlank()) {
                    throw new AuthenticationException("Le serveur n'a pas retourné de token.");
                }
                TokenManager.getInstance().setToken(dto.getAccessToken());
                TokenManager.getInstance().setRefreshToken(dto.getRefreshToken());
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new AuthenticationException("Email ou mot de passe incorrect.");
            } else {
                throw new AuthenticationException(
                        "Erreur serveur (HTTP " + response.statusCode() + "). Veuillez réessayer.");
            }
        } catch (IOException e) {
            throw new AuthenticationException("Impossible de contacter le serveur : " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException("La requête a été interrompue.");
        }
    }

    /**
     * Exchanges the stored refresh token for a new token pair.
     * Updates {@link TokenManager} with the new access and refresh tokens.
     *
     * @throws AuthenticationException if no refresh token is stored, the token is invalid/expired,
     *                                 or the server is unreachable
     */
    public void refreshTokens() throws AuthenticationException {
        String refreshToken = TokenManager.getInstance().getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthenticationException("No refresh token available. Please log in again.");
        }

        String jsonBody = String.format("{\"refreshToken\":\"%s\"}", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/refresh"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Client-Type", "Desktop")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                LoginResponseDto dto = objectMapper.readValue(response.body(), LoginResponseDto.class);
                if (dto.getAccessToken() == null || dto.getAccessToken().isBlank()) {
                    throw new AuthenticationException("Le serveur n'a pas retourné de token.");
                }
                TokenManager.getInstance().setToken(dto.getAccessToken());
                TokenManager.getInstance().setRefreshToken(dto.getRefreshToken());
            } else if (response.statusCode() == 401) {
                TokenManager.getInstance().clear();
                throw new AuthenticationException("Session expirée. Veuillez vous reconnecter.");
            } else {
                throw new AuthenticationException(
                        "Erreur serveur (HTTP " + response.statusCode() + "). Veuillez réessayer.");
            }
        } catch (IOException e) {
            throw new AuthenticationException("Impossible de contacter le serveur : " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException("La requête a été interrompue.");
        }
    }

    /**
     * Clear the stored tokens (logout).
     */
    public void logout() {
        TokenManager.getInstance().clear();
    }

    /**
     * Exception thrown when authentication fails.
     */
    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
