package it.ute.QAUTE.service.Implement;


import it.ute.QAUTE.dto.UserReportDTO;
import it.ute.QAUTE.repository.UserRepository;
import it.ute.QAUTE.service.UserReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class UserReportServiceImplement implements UserReportService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Long getTotalUsers(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countAllUsers(startDate, endDate);
    }

    @Override
    public List<UserReportDTO> getUsersByRole(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.getUsersByRole(startDate, endDate);
    }

    @Override
    public Long getActiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);  // 30 ngay gan nhat
        return userRepository.countActiveUsers(cutoff);
    }

    @Override
    public List<UserReportDTO> getTop10Users(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.getTopUsersByQuestions(PageRequest.of(0, 10));
    }

    @Override
    public double userChange() {
        LocalDateTime now = LocalDateTime.now();

        YearMonth thisMonth = YearMonth.from(now);
        LocalDateTime startOfThisMonth = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfThisMonth = thisMonth.atEndOfMonth().atTime(LocalTime.MAX);

        YearMonth lastMonth = thisMonth.minusMonths(1);
        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(LocalTime.MAX);

        long newCount = userRepository.countAllUsers(startOfThisMonth, endOfThisMonth);
        long oldCount = userRepository.countAllUsers(startOfLastMonth, endOfLastMonth);

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
