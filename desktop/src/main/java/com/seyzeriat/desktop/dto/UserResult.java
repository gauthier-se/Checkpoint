package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a user returned by the admin API.
 * Maps the JSON response from {@code GET /api/admin/users}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResult {

    private String id;
    private String username;
    private String email;

    public UserResult() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
