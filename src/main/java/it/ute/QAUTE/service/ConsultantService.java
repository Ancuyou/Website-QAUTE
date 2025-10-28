package it.ute.QAUTE.service;

import it.ute.QAUTE.dto.ConsultantDTO;
import it.ute.QAUTE.entity.Consultant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultantService {
    List<ConsultantDTO> getAllConsultants();

    Optional<Consultant> findByProfileId(Integer profileId);

    List<Consultant> findAllConsultants();

    void updateConsultant(Consultant consultant);

    void saveConsultant(Consultant consultant);

    List<ConsultantDTO> getConsultantsWithSortingAndFilter(
            String sortBy,
            String timeRange);

    // Hàm hỗ trợ tính ngày bắt đầu
    LocalDateTime calculateStartDate(String timeRange);

    // Hàm hỗ trợ sắp xếp danh sách DTO
    void sortConsultants(List<ConsultantDTO> dtos, String sortBy);
}
