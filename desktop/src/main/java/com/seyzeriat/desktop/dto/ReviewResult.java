package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a user review returned by the admin API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewResult {
    private String id;
    private String content;
    private Boolean haveSpoilers;
    private String authorUsername;
    private String gameTitle;
    private String createdAt;
    private long reportCount;

    public ReviewResult() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getHaveSpoilers() { return haveSpoilers; }
    public void setHaveSpoilers(Boolean haveSpoilers) { this.haveSpoilers = haveSpoilers; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public long getReportCount() { return reportCount; }
    public void setReportCount(long reportCount) { this.reportCount = reportCount; }
}
