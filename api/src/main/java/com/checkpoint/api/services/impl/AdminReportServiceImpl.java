package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.admin.AdminReportDetailDto;
import com.checkpoint.api.dto.admin.AdminReportDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.entities.Report;
import com.checkpoint.api.exceptions.ReportNotFoundException;
import com.checkpoint.api.repositories.ReportRepository;
import com.checkpoint.api.services.AdminReportService;

/**
 * Implementation of {@link AdminReportService} for admin report management operations.
 */
@Service
@Transactional
public class AdminReportServiceImpl implements AdminReportService {

    private static final Logger log = LoggerFactory.getLogger(AdminReportServiceImpl.class);

    private final ReportRepository reportRepository;

    public AdminReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<AdminReportDto> getAllReports(Pageable pageable, String type) {
        log.info("Fetching all reports for admin (pageable = {}, type = {})", pageable, type);

        Page<Report> reportsPage;

        if ("review".equalsIgnoreCase(type)) {
            reportsPage = reportRepository.findByReviewIsNotNull(pageable);
        } else if ("comment".equalsIgnoreCase(type)) {
            reportsPage = reportRepository.findByCommentIsNotNull(pageable);
        } else {
            reportsPage = reportRepository.findAll(pageable);
        }

        Page<AdminReportDto> dtoPage = reportsPage.map(this::mapToAdminReportDto);

        return PagedResponseDto.from(dtoPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AdminReportDetailDto getReportById(UUID reportId) {
        log.info("Fetching report detail for admin (reportId = {})", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        return mapToAdminReportDetailDto(report);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissReport(UUID reportId) {
        log.info("Dismissing report with id: {}", reportId);

        if (!reportRepository.existsById(reportId)) {
            throw new ReportNotFoundException(reportId);
        }

        reportRepository.deleteById(reportId);
    }

    private AdminReportDto mapToAdminReportDto(Report report) {
        String type = report.getReview() != null ? "review" : "comment";
        String contentPreview = resolveContentPreview(report);

        return new AdminReportDto(
                report.getId(),
                report.getUser() != null ? report.getUser().getPseudo() : null,
                report.getContent(),
                type,
                contentPreview,
                report.getCreatedAt()
        );
    }

    private AdminReportDetailDto mapToAdminReportDetailDto(Report report) {
        String type = report.getReview() != null ? "review" : "comment";

        UUID targetId;
        String targetAuthorUsername;
        String targetFullContent;

        if (report.getReview() != null) {
            targetId = report.getReview().getId();
            targetAuthorUsername = report.getReview().getUser() != null
                    ? report.getReview().getUser().getPseudo() : null;
            targetFullContent = report.getReview().getContent();
        } else {
            targetId = report.getComment() != null ? report.getComment().getId() : null;
            targetAuthorUsername = report.getComment() != null && report.getComment().getUser() != null
                    ? report.getComment().getUser().getPseudo() : null;
            targetFullContent = report.getComment() != null ? report.getComment().getContent() : null;
        }

        return new AdminReportDetailDto(
                report.getId(),
                report.getUser() != null ? report.getUser().getPseudo() : null,
                report.getContent(),
                type,
                targetId,
                targetAuthorUsername,
                targetFullContent,
                report.getCreatedAt()
        );
    }

    private String resolveContentPreview(Report report) {
        String fullContent;

        if (report.getReview() != null) {
            fullContent = report.getReview().getContent();
        } else if (report.getComment() != null) {
            fullContent = report.getComment().getContent();
        } else {
            return null;
        }

        if (fullContent != null && fullContent.length() > 100) {
            return fullContent.substring(0, 100) + "...";
        }

        return fullContent;
    }
}
