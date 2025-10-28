package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {
    void save(Report report);

    Page<Report> searchReports(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String contentType,
            Report.ReportReason reason,
            Report.ReportStatus status,
            Pageable pageable);

    void populateContentDetails(List<Report> reports);

    long countByStatus(Report.ReportStatus status);

    @Transactional
    void approveReport(Long reportId, String contentType, Long contentId);

    @Transactional
    void rejectReport(Long reportId);
}
