package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.dto.QuestionReportDTO;
import it.ute.QAUTE.repository.QuestionRepository;
import it.ute.QAUTE.service.QuestionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class QuestionReportServiceImplement implements QuestionReportService {
    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public long getTotalQuestions(LocalDateTime start, LocalDateTime end) {
        return questionRepository.countAllQuestions(start, end);
    }

    @Override
    public List<QuestionReportDTO> getByField(LocalDateTime start, LocalDateTime end) {
        return questionRepository.getQuestionsByField(start, end);
    }

    @Override
    public List<QuestionReportDTO> getByDepartment(LocalDateTime start, LocalDateTime end) {
        return questionRepository.getQuestionsByDepartment(start, end);
    }

    @Override
    public List<QuestionReportDTO> getByStatus(LocalDateTime start, LocalDateTime end) {
        return questionRepository.getQuestionsByStatus(start, end);
    }

    @Override
    public List<QuestionReportDTO> getByDate(LocalDateTime start, LocalDateTime end) {
        return questionRepository.getQuestionsByDate(start, end);
    }

    @Override
    public double questionChange() {
        LocalDateTime now = LocalDateTime.now();

        YearMonth thisMonth = YearMonth.from(now);
        LocalDateTime startOfThisMonth = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfThisMonth = thisMonth.atEndOfMonth().atTime(LocalTime.MAX);

        YearMonth lastMonth = thisMonth.minusMonths(1);
        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(LocalTime.MAX);

        long newCount = questionRepository.countAllQuestions(startOfThisMonth, endOfThisMonth);
        long oldCount = questionRepository.countAllQuestions(startOfLastMonth, endOfLastMonth);

        if (oldCount == 0) {
            if (newCount > 0) {
                return 100.0;
            } else {
                return 0.0;
            }
        }
        return ((double) (newCount - oldCount) / oldCount) * 100.0;
    }
}
