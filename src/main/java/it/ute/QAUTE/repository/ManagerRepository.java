package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager,Integer> {
    Manager findByProfile_ProfileID(int profileID);
}
