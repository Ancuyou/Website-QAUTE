package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Answer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    @Query("SELECT a FROM Answer a WHERE a.consultant.consultantID = :consultantId")
    List<Answer> findByConsultant_ConsultantID(Integer consultantId);
}