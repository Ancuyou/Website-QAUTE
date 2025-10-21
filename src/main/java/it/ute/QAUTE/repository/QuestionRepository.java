package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @Query("""
        SELECT q
        FROM Question q
        JOIN q.user u
        JOIN u.profile p
        WHERE q.department.departmentID = :departmentId
          AND q.field.fieldID = :fieldId
          AND q.status = :status
    """)
    Page<Question> findQuestionsByDeptAndField(
            @Param("departmentId") Integer departmentId,
            @Param("fieldId") Integer fieldId,
            @Param("status") Question.QuestionStatus status,
            Pageable pageable);


    @Query("""
    SELECT q
    FROM Question q
    JOIN q.user u
    JOIN u.profile p
    WHERE p.fullName LIKE CONCAT('%', :username, '%')
    """)
    Page<Question> findQuestionsByUserName(@Param("username") String username,
                                           Pageable pageable);

    @Query("""
        SELECT q
        FROM Question q
        JOIN q.user u
        JOIN u.profile p
        WHERE q.status = :status
    """)
    Page<Question> findQuestionsByStatus(
            @Param("status") Question.QuestionStatus status,
            Pageable pageable);


    @Query("""
        SELECT q
        FROM Question q
        JOIN q.user u
        JOIN u.profile p
        WHERE q.department.departmentID = :departmentId
          AND q.status = :status
    """)
    Page<Question> findQuestionsByDeptAndStatus(
            @Param("departmentId") Integer departmentId,
            @Param("status") Question.QuestionStatus status,
            Pageable pageable);

    @Query("""
        SELECT q
        FROM Question q
        JOIN q.user u
        JOIN u.profile p
        WHERE q.department.departmentID = :departmentId
    """)
    Page<Question> findQuestionsByDept(
            @Param("departmentId") Integer departmentId,
            Pageable pageable);

    @Query("""
    SELECT q
    FROM Question q
    JOIN q.user u
    JOIN u.profile p
    """)
    Page<Question> findAllWithUser(Pageable pageable);
}