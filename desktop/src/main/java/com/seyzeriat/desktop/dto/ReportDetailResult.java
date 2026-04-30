package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing the full details of a report returned by the admin API,
 * including the ID and content of the reported target (review or comment).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDetailResult {
    private String id;
    private String reporterUsername;
    private String reason;
    private String type;
    private String targetId;
    private String targetAuthorUsername;
    private String targetFullContent;
    private String createdAt;

    public ReportDetailResult() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getTargetAuthorUsername() { return targetAuthorUsername; }
    public void setTargetAuthorUsername(String targetAuthorUsername) { this.targetAuthorUsername = targetAuthorUsername; }

    public String getTargetFullContent() { return targetFullContent; }
    public void setTargetFullContent(String targetFullContent) { this.targetFullContent = targetFullContent; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
