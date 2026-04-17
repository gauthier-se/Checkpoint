package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a single report filed against a review,
 * returned by the admin API when inspecting a review's reports.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewReportResult {
    private String id;
    private String reporterUsername;
    private String reason;
    private String createdAt;

    public ReviewReportResult() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
