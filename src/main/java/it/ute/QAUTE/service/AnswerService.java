package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Answer;
import it.ute.QAUTE.repository.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    public void saveAnswer(Answer answer) {
        answerRepository.save(answer);
    }
}