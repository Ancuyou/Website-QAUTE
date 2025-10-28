package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ToxicContentService {
    Page<Question> findToxicQuestionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    void rejectedQuestion(Integer id);

    void approvedQuestion(Integer id);
}
