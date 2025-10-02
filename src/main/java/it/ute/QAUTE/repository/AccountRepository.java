package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Account findUserByEmail(String email);
    Account findByUsername(String username);
    Account findByEmail(String email);
}
