package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.admin.AdminReportDetailDto;
import com.checkpoint.api.dto.admin.AdminReportDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;

/**
 * Service interface for admin report management operations.
 */
public interface AdminReportService {

    /**
     * Retrieves a paginated list of all reports, optionally filtered by type.
     *
     * @param pageable pagination and sorting details
     * @param type     optional filter: "review" or "comment" (null for all)
     * @return the paginated reports
     */
    PagedResponseDto<AdminReportDto> getAllReports(Pageable pageable, String type);

    /**
     * Retrieves the full details of a specific report.
     *
     * @param reportId the report ID
     * @return the report details including reporter, reason, and reported content
     */
    AdminReportDetailDto getReportById(UUID reportId);

    /**
     * Dismisses (deletes) a report by its ID.
     *
     * @param reportId the ID of the report to dismiss
     */
    void dismissReport(UUID reportId);
}
