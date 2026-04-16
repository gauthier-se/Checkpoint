package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.admin.AdminReportDetailDto;
import com.checkpoint.api.dto.admin.AdminReportDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.entities.Comment;
import com.checkpoint.api.entities.Report;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.ReportNotFoundException;
import com.checkpoint.api.repositories.ReportRepository;
import com.checkpoint.api.services.impl.AdminReportServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    private AdminReportServiceImpl adminReportService;

    @BeforeEach
    void setUp() {
        adminReportService = new AdminReportServiceImpl(reportRepository);
    }

    private User createUser(String pseudo) {
        User user = new User();
        user.setPseudo(pseudo);
        return user;
    }

    private Review createReview(UUID id, String content, User author) {
        Review review = new Review();
        review.setId(id);
        review.setContent(content);
        review.setUser(author);
        return review;
    }

    private Comment createComment(UUID id, String content, User author) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setUser(author);
        return comment;
    }

    private Report createReviewReport(UUID id, String reason, User reporter, Review review) {
        Report report = new Report(reason, reporter);
        report.setId(id);
        report.setReview(review);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private Report createCommentReport(UUID id, String reason, User reporter, Comment comment) {
        Report report = new Report(reason, reporter);
        report.setId(id);
        report.setComment(comment);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    @Test
    @DisplayName("getAllReports should return all reports when no type filter")
    void getAllReports_shouldReturnAllReportsWhenNoTypeFilter() {
        // Given
        UUID reportId = UUID.randomUUID();
        User reporter = createUser("reporter1");
        User author = createUser("author1");
        Review review = createReview(UUID.randomUUID(), "Bad review", author);
        Report report = createReviewReport(reportId, "Spam", reporter, review);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(List.of(report), pageable, 1);
        when(reportRepository.findAll(pageable)).thenReturn(reportPage);

        // When
        PagedResponseDto<AdminReportDto> result = adminReportService.getAllReports(pageable, null);

        // Then
        assertThat(result.content()).hasSize(1);
        AdminReportDto dto = result.content().getFirst();
        assertThat(dto.id()).isEqualTo(reportId);
        assertThat(dto.reporterUsername()).isEqualTo("reporter1");
        assertThat(dto.reason()).isEqualTo("Spam");
        assertThat(dto.type()).isEqualTo("review");
        assertThat(dto.contentPreview()).isEqualTo("Bad review");

        verify(reportRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getAllReports should filter by review type")
    void getAllReports_shouldFilterByReviewType() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(reportRepository.findByReviewIsNotNull(pageable)).thenReturn(emptyPage);

        // When
        PagedResponseDto<AdminReportDto> result = adminReportService.getAllReports(pageable, "review");

        // Then
        assertThat(result.content()).isEmpty();
        verify(reportRepository).findByReviewIsNotNull(pageable);
    }

    @Test
    @DisplayName("getAllReports should filter by comment type")
    void getAllReports_shouldFilterByCommentType() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(reportRepository.findByCommentIsNotNull(pageable)).thenReturn(emptyPage);

        // When
        PagedResponseDto<AdminReportDto> result = adminReportService.getAllReports(pageable, "comment");

        // Then
        assertThat(result.content()).isEmpty();
        verify(reportRepository).findByCommentIsNotNull(pageable);
    }

    @Test
    @DisplayName("getAllReports should truncate long content preview to 100 characters")
    void getAllReports_shouldTruncateLongContentPreview() {
        // Given
        UUID reportId = UUID.randomUUID();
        User reporter = createUser("reporter1");
        User author = createUser("author1");
        String longContent = "A".repeat(150);
        Review review = createReview(UUID.randomUUID(), longContent, author);
        Report report = createReviewReport(reportId, "Spam", reporter, review);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(List.of(report), pageable, 1);
        when(reportRepository.findAll(pageable)).thenReturn(reportPage);

        // When
        PagedResponseDto<AdminReportDto> result = adminReportService.getAllReports(pageable, null);

        // Then
        assertThat(result.content().getFirst().contentPreview()).hasSize(103); // 100 + "..."
        assertThat(result.content().getFirst().contentPreview()).endsWith("...");
    }

    @Test
    @DisplayName("getReportById should return report detail for review report")
    void getReportById_shouldReturnReportDetailForReviewReport() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        User reporter = createUser("reporter1");
        User author = createUser("author1");
        Review review = createReview(reviewId, "Offensive review content", author);
        Report report = createReviewReport(reportId, "Inappropriate", reporter, review);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // When
        AdminReportDetailDto result = adminReportService.getReportById(reportId);

        // Then
        assertThat(result.id()).isEqualTo(reportId);
        assertThat(result.reporterUsername()).isEqualTo("reporter1");
        assertThat(result.reason()).isEqualTo("Inappropriate");
        assertThat(result.type()).isEqualTo("review");
        assertThat(result.targetId()).isEqualTo(reviewId);
        assertThat(result.targetAuthorUsername()).isEqualTo("author1");
        assertThat(result.targetFullContent()).isEqualTo("Offensive review content");

        verify(reportRepository).findById(reportId);
    }

    @Test
    @DisplayName("getReportById should return report detail for comment report")
    void getReportById_shouldReturnReportDetailForCommentReport() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User reporter = createUser("reporter1");
        User author = createUser("commenter1");
        Comment comment = createComment(commentId, "Bad comment content", author);
        Report report = createCommentReport(reportId, "Hate speech", reporter, comment);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // When
        AdminReportDetailDto result = adminReportService.getReportById(reportId);

        // Then
        assertThat(result.id()).isEqualTo(reportId);
        assertThat(result.type()).isEqualTo("comment");
        assertThat(result.targetId()).isEqualTo(commentId);
        assertThat(result.targetAuthorUsername()).isEqualTo("commenter1");
        assertThat(result.targetFullContent()).isEqualTo("Bad comment content");

        verify(reportRepository).findById(reportId);
    }

    @Test
    @DisplayName("getReportById should throw ReportNotFoundException when not found")
    void getReportById_shouldThrowWhenNotFound() {
        // Given
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> adminReportService.getReportById(reportId))
                .isInstanceOf(ReportNotFoundException.class);

        verify(reportRepository).findById(reportId);
    }

    @Test
    @DisplayName("dismissReport should delete when report exists")
    void dismissReport_shouldDeleteWhenReportExists() {
        // Given
        UUID reportId = UUID.randomUUID();
        when(reportRepository.existsById(reportId)).thenReturn(true);

        // When
        adminReportService.dismissReport(reportId);

        // Then
        verify(reportRepository).existsById(reportId);
        verify(reportRepository).deleteById(reportId);
    }

    @Test
    @DisplayName("dismissReport should throw ReportNotFoundException when not found")
    void dismissReport_shouldThrowWhenNotFound() {
        // Given
        UUID reportId = UUID.randomUUID();
        when(reportRepository.existsById(reportId)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> adminReportService.dismissReport(reportId))
                .isInstanceOf(ReportNotFoundException.class);

        verify(reportRepository).existsById(reportId);
    }
}
