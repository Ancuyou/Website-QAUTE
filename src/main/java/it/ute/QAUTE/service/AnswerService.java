package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Answer;
import it.ute.QAUTE.repository.AnswerRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    public void saveAnswer(Answer answer) {
        answerRepository.save(answer);
    }

    public List<Answer> getAllAnswersByConsultant(Integer consultantId) {
        return answerRepository.findByConsultant_ConsultantID(consultantId);
    }
}