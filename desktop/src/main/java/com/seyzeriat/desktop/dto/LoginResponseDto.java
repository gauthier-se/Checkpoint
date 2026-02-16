package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing the JWT login response from the API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponseDto {

    private String token;

    public LoginResponseDto() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
