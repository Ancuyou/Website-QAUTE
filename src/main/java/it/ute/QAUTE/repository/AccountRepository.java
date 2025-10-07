package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Account findUserByEmail(String email);
    Account findByUsername(String username);
    Account findByEmail(String email);
    
    // Tìm tài khoản theo profileID
    @Query("SELECT a FROM Account a WHERE a.profile.profileID = :profileId")
    Account findByProfile_ProfileID(@Param("profileId") Integer profileId);
}
