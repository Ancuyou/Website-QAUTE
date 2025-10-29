package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.entity.Answer;
import it.ute.QAUTE.entity.Consultant;
import it.ute.QAUTE.exception.AppException;
import it.ute.QAUTE.exception.ErrorCode;
import it.ute.QAUTE.repository.AnswerRepository;

import java.time.LocalDateTime;
import java.util.List;

import it.ute.QAUTE.service.AnswerService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return answerRepository.countVisibleAnswersByQuestionUser(user);
    }
    @Override
    public long countAll() {
        return answerRepository.count();
    }

    @Override
    @Transactional
    public void withdrawAnswer(Integer answerId, Consultant currentConsultant) {
        Answer answer = findById(answerId);
        if (!(answer.getConsultant().getConsultantID() == currentConsultant.getConsultantID())) {
            throw new AppException(ErrorCode.CANNOT_WITHDRAW_ANSWER);
        }
        if (answer.isWithdrawn()) {
            throw new AppException(ErrorCode.CANNOT_WITHDRAW_ANSWER);
        }
        answer.setWithdrawn(true);
        answerRepository.save(answer);
    }

    @Override
    public Answer findById(Integer answerId) {
        Optional<Answer> answerOptional = answerRepository.findById(answerId);
        if (answerOptional.isEmpty()) {
            throw new AppException(ErrorCode.ANSWER_NOT_FOUND);
        }
        return answerOptional.get();
    }
}