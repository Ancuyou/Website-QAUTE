package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Consultant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Integer> {
    
    @Query("SELECT c FROM Consultant c JOIN FETCH c.profile p ORDER BY p.fullName")
    List<Consultant> findAllWithProfiles();
    
    Consultant findByProfile_ProfileID(Integer profileID);
}