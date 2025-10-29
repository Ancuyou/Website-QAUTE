package it.ute.QAUTE.service;

import it.ute.QAUTE.dto.HotTopicDTO;
import it.ute.QAUTE.dto.QuestionDTO;
import it.ute.QAUTE.entity.Department;
import it.ute.QAUTE.entity.Field;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface QuestionService {
    List<Question> getAllQuestions();

    void deleteQuestion(Integer id);

    List<Department> getAllDepartments();

    List<Field> getAllFields();

    void saveQuestion(Question question);

    Question getQuestionById(Integer questionId);

    Page<Question> filterQuestions(Integer departmentId, Integer fieldId, String userName, String status, Pageable pageable);

    Question findById(Integer questionId);

    Question save(Question question);

    List<HotTopicDTO> getTop5HotTopics();

    long countQuestionsByUser(it.ute.QAUTE.entity.User user);

    List<Question> getTop3RecentQuestionsByUser(it.ute.QAUTE.entity.User user);

    List<Question> getTop5RecentCommunityQuestions();

    List<Question> getAllQuestionsByUserSortedByDate(it.ute.QAUTE.entity.User user);

    List<Field> getFieldsByDepartmentId(Integer departmentId);

    Page<Question> searchAndFilterQuestions(
            Integer departmentId,
            Integer fieldId,
            String keyword,
            String sortBy,
            Pageable pageable);

    Question findQuestionByIdForEditing(Integer questionId, int userId);

    @Transactional
    void updateQuestion(Integer questionId, int userId, QuestionDTO questionDTO);

    Page<Question> searchAndFilterUserQuestions(
            User user, // Thêm tham số user
            Integer departmentId,
            Integer fieldId,
            String keyword,
            String sortBy,
            Pageable pageable);

    long countAll();

    long countByStatus(Question.QuestionStatus status);

    long countAnwer_Questions(LocalDateTime start, LocalDateTime end);

    long countByIsToxic();
}
