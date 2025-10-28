package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.entity.Answer;
import it.ute.QAUTE.repository.AnswerRepository;

import java.time.LocalDateTime;
import java.util.List;

import it.ute.QAUTE.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImplement implements AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Override
    public void saveAnswer(Answer answer) {
        answerRepository.save(answer);
    }

    @Override
    public List<Answer> getAllAnswersByConsultant(Integer consultantId) {
        return answerRepository.findByConsultant_ConsultantID(consultantId);
    }

    @Override
    public Page<Answer> getAnswersHistoryByConsultant(Integer consultantId, Integer departmentId, Integer fieldId, Integer timeRange, String keyword, Pageable pageable) {
        LocalDateTime cutoffDate = null;
        if (timeRange != null) {
            cutoffDate = LocalDateTime.now().minusDays(timeRange);
        }
        return answerRepository.findAnswersHistoryByConsultant(consultantId, departmentId, fieldId, cutoffDate, keyword, pageable);
    }

    @Override
    public List<Answer> getAnswersByQuestionId(Integer questionId) {
        return answerRepository.findByQuestion_QuestionID(questionId);
    }

    @Override
    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }
    @Override
    public long countAnswersForUser(it.ute.QAUTE.entity.User user) {
        return answerRepository.countByQuestionUser(user);
    }
    @Override
    public long countAll() {
        return answerRepository.count();
    }
}