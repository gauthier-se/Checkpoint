package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing the token pair login response from the API.
 * The API returns {@code accessToken} (JWT, 24h) and {@code refreshToken} (opaque, 7d).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponseDto {

    private String accessToken;
    private String refreshToken;

    public LoginResponseDto() {}

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
