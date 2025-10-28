package it.ute.QAUTE.service;

import it.ute.QAUTE.dto.QuestionReportDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface QuestionReportService {
    long getTotalQuestions(LocalDateTime start, LocalDateTime end);

    List<QuestionReportDTO> getByField(LocalDateTime start, LocalDateTime end);

    List<QuestionReportDTO> getByDepartment(LocalDateTime start, LocalDateTime end);

    List<QuestionReportDTO> getByStatus(LocalDateTime start, LocalDateTime end);

    List<QuestionReportDTO> getByDate(LocalDateTime start, LocalDateTime end);

    double questionChange();
}
