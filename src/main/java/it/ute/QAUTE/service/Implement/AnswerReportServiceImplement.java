package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.dto.AnswerReportDTO;
import it.ute.QAUTE.repository.AnswerRepository;
import it.ute.QAUTE.service.AnswerReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class AnswerReportServiceImplement implements AnswerReportService {
    @Autowired
    private AnswerRepository answerRepository;

    @Override
    public long getTotalAnswers(LocalDateTime startDate, LocalDateTime endDate) {
        return answerRepository.countAllAnswers(startDate, endDate);
    }

    @Override
    public double getAverageResponseTime(LocalDateTime startDate, LocalDateTime endDate) {
        Double avg = answerRepository.averageResponseTime(startDate, endDate);
        return avg != null ? avg : 0.0;
    }

    @Override
    public List<AnswerReportDTO> getAnswersByConsultant(LocalDateTime startDate, LocalDateTime endDate) {
        return answerRepository.getAnswersByConsultant(startDate, endDate);
    }

    @Override
    public List<AnswerReportDTO> getAnswersByDate(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = answerRepository.getAnswersByDateRaw(startDate, endDate);
        return results.stream()
                .map(r -> new AnswerReportDTO(
                        null,
                        ((Number) r[1]).longValue(),
                        ((java.sql.Date) r[0]).toLocalDate()
                ))
                .toList();
    }

    @Override
    public double answerChange() {
        LocalDateTime now = LocalDateTime.now();

        YearMonth thisMonth = YearMonth.from(now);
        LocalDateTime startOfThisMonth = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfThisMonth = thisMonth.atEndOfMonth().atTime(LocalTime.MAX);

        YearMonth lastMonth = thisMonth.minusMonths(1);
        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(LocalTime.MAX);

        long newCount = answerRepository.countAllAnswers(startOfThisMonth, endOfThisMonth);
        long oldCount = answerRepository.countAllAnswers(startOfLastMonth, endOfLastMonth);

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
