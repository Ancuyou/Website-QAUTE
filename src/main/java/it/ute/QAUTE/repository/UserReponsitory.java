package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReponsitory extends JpaRepository<User, String> {
    User findByUsername(String username);
    boolean existsByEmail(String email);
    User findByEmail(String email);
}
