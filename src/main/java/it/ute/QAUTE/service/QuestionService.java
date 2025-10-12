package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Department;
import it.ute.QAUTE.entity.Field;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.repository.DepartmentRepository;
import it.ute.QAUTE.repository.FieldRepository;
import it.ute.QAUTE.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private FieldRepository fieldRepository;

    public List<Question> getAllQuestions() {
        return questionRepository.findAll(Sort.by(Sort.Direction.DESC, "dateSend"));
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Field> getAllFields() {
        return fieldRepository.findAll();
    }

    public void saveQuestion(Question question) {
        questionRepository.save(question);
    }
    
    public Question getQuestionById(Integer questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại với ID: " + questionId));
    }
}