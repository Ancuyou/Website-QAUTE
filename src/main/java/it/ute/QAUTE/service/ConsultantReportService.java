package it.ute.QAUTE.service;

import it.ute.QAUTE.dto.ConsultantReportDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultantReportService {
    long getTotalConsultants();

    List<ConsultantReportDTO> getPerformance(LocalDateTime startDate, LocalDateTime endDate);
}
