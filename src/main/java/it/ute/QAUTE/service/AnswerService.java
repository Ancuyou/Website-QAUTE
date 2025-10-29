package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Answer;
import it.ute.QAUTE.entity.Consultant;

import it.ute.QAUTE.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnswerService {
    void saveAnswer(Answer answer);

    List<Answer> getAllAnswersByConsultant(Integer consultantId);

    Page<Answer> getAnswersHistoryByConsultant(Integer consultantId, Integer departmentId, Integer fieldId, Integer timeRange, String keyword, Pageable pageable);

    List<Answer> getAnswersByQuestionId(Integer questionId);

    List<Answer> getAllAnswers();

    long countAnswersForUser(it.ute.QAUTE.entity.User user);

    long countAll();

    void withdrawAnswer(Integer answerId, Consultant currentConsultant);

    Answer findById(Integer answerId);

    int countByQuestionAndNotWithdrawn(Question question);
}
