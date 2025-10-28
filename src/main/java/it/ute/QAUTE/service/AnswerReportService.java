package it.ute.QAUTE.service;

import it.ute.QAUTE.dto.AnswerReportDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AnswerReportService {
    long getTotalAnswers(LocalDateTime startDate, LocalDateTime endDate);

    double getAverageResponseTime(LocalDateTime startDate, LocalDateTime endDate);

    List<AnswerReportDTO> getAnswersByConsultant(LocalDateTime startDate, LocalDateTime endDate);

    List<AnswerReportDTO> getAnswersByDate(LocalDateTime startDate, LocalDateTime endDate);

    double answerChange();
}
