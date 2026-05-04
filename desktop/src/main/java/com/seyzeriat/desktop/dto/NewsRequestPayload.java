package com.seyzeriat.desktop.dto;

/**
 * Payload sent to the admin API when creating or updating a news article.
 *
 * <p>Mirrors the API's {@code NewsRequestDto}: title, description (markdown
 * content) and picture (cover image URL). The author is derived from the JWT
 * server-side and is therefore not part of this payload.</p>
 */
public class NewsRequestPayload {
    private String title;
    private String description;
    private String picture;

    public NewsRequestPayload() {}

    public NewsRequestPayload(String title, String description, String picture) {
        this.title = title;
        this.description = description;
        this.picture = picture;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
}
