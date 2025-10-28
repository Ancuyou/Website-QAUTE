package it.ute.QAUTE.service;

import it.ute.QAUTE.dto.UserReportDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface UserReportService {
    Long getTotalUsers(LocalDateTime startDate, LocalDateTime endDate);

    List<UserReportDTO> getUsersByRole(LocalDateTime startDate, LocalDateTime endDate);

    Long getActiveUsers();

    List<UserReportDTO> getTop10Users(LocalDateTime startDate, LocalDateTime endDate);

    double userChange();
}
