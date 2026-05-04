package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a news article returned by the admin API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsResult {
    private String id;
    private String title;
    private String description;
    private String picture;
    private String publishedAt;
    private String createdAt;
    private String updatedAt;
    private NewsAuthorResult author;

    public NewsResult() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public NewsAuthorResult getAuthor() { return author; }
    public void setAuthor(NewsAuthorResult author) { this.author = author; }

    public boolean isPublished() {
        return publishedAt != null && !publishedAt.isBlank();
    }
}
