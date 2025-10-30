package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin,Integer> {
    Admin findByProfile_ProfileID(int profileID);
}
