package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // Đếm số câu hỏi của một user
    long countByUser(User user);
    // Lấy 3 câu hỏi gần nhất của một user
    List<Question> findTop3ByUserOrderByDateSendDesc(User user);
    // Lấy các câu hỏi mới nhất từ cộng đồng để hiển thị
    List<Question> findTop5ByOrderByDateSendDesc();
}