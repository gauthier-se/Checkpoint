package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing an external game search result from the API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalGameResult {
    private Long externalId;
    private String title;
    private Integer releaseYear;
    private String coverUrl;

    public ExternalGameResult() {}

    public Long getExternalId() { return externalId; }
    public void setExternalId(Long externalId) { this.externalId = externalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
}
