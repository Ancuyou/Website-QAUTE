package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Department;
import it.ute.QAUTE.entity.Field;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.repository.DepartmentRepository;
import it.ute.QAUTE.repository.FieldRepository;
import it.ute.QAUTE.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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

    public void deleteQuestion(Integer id) {
        questionRepository.deleteById(id);
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

    public Page<Question> filterQuestions(Integer departmentId, Integer fieldId, String userName, String status, Pageable pageable) {
        boolean hasDept = departmentId != null;
        boolean hasField = fieldId != null;
        boolean hasUser = userName != null && !userName.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();

        Question.QuestionStatus statusEnum = null;
        if (hasStatus) {
            statusEnum = Question.QuestionStatus.valueOf(status);
        }

        if (hasUser) {
            return questionRepository.findQuestionsByUserName(userName, pageable);
        }

        if (hasDept && hasField && hasStatus) {
            return questionRepository.findQuestionsByDeptAndField(departmentId, fieldId, statusEnum, pageable);
        }
        if (hasDept && !hasField && hasStatus) {
            return questionRepository.findQuestionsByDeptAndStatus(departmentId, statusEnum, pageable);
        }
        if (hasDept && !hasField) {
            return questionRepository.findQuestionsByDept(departmentId, pageable);
        }
        if (!hasDept && !hasField && hasStatus) {
            return questionRepository.findQuestionsByStatus(statusEnum, pageable);
        }

        return questionRepository.findAllWithUser(pageable);
    }


    public Question findById(Integer questionId) {
        return questionRepository.findById(questionId).orElse(null);
    }

    public Question save(Question question) {
        return questionRepository.save(question);
    }
}