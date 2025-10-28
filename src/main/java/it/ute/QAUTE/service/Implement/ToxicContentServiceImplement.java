package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.exception.AppException;
import it.ute.QAUTE.exception.ErrorCode;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.repository.QuestionRepository;
import it.ute.QAUTE.service.ToxicContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ToxicContentServiceImplement implements ToxicContentService {

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public Page<Question> findToxicQuestionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if  (startDate == null || endDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
            endDate = LocalDateTime.now();
        }
        return questionRepository.findToxicQuestionsByDateRange(startDate, endDate, pageable);
    }

    @Override
    public void rejectedQuestion(Integer id) {
        Question question = questionRepository.findById(id).orElseThrow( () -> new AppException(ErrorCode.QUESTION_UNEXISTED));
        question.setStatus(Question.QuestionStatus.Rejected);
        questionRepository.save(question);
    }

    @Override
    public void approvedQuestion(Integer id) {
        Question question = questionRepository.findById(id).orElseThrow( () -> new AppException(ErrorCode.QUESTION_UNEXISTED));
        question.setStatus(Question.QuestionStatus.Approved);
        question.setToxic(true);
        questionRepository.save(question);
    }

}
