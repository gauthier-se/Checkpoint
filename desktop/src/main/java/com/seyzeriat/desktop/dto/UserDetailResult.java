package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a detailed user profile returned by the admin API.
 * Maps the JSON response from {@code GET /api/admin/users/{id}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailResult {

    private String id;
    private String username;
    private String email;
    private String bio;
    private String picture;
    private boolean isPrivate;
    private boolean banned;
    private int xpPoint;
    private int level;
    private String createdAt;
    private long reviewCount;
    private long reportCount;

    public UserDetailResult() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }

    public int getXpPoint() { return xpPoint; }
    public void setXpPoint(int xpPoint) { this.xpPoint = xpPoint; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public long getReviewCount() { return reviewCount; }
    public void setReviewCount(long reviewCount) { this.reviewCount = reviewCount; }

    public long getReportCount() { return reportCount; }
    public void setReportCount(long reportCount) { this.reportCount = reportCount; }
}
