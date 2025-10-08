package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Account findUserByEmail(String email);
    Account findByUsername(String username);
    Account findByEmail(String email);

    @EntityGraph(attributePaths = "profile")
    @Query("SELECT a FROM Account a WHERE a.role!='Admin'")
    Page<Account> findAllWithProfile(Pageable pageable);

    @Query("SELECT a FROM Account a JOIN a.profile p WHERE a.role != 'Admin' AND " +
            "(LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Account> searchByKeywordExceptAdmin(String keyword, Pageable pageable);

    @Query("SELECT a FROM Account a JOIN a.profile p WHERE a.role != 'Admin' AND a.role=:role AND " +
            "(LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Account> searchByKeywordAndRole(String keyword, Account.Role role, Pageable pageable);

    @Query("SELECT a FROM Account a JOIN a.profile p WHERE a.role != 'Admin' AND a.role=:role")
    Page<Account> searchByRole(Account.Role role, Pageable pageable);

    @EntityGraph(attributePaths = "profile")
    @Query("SELECT a FROM Account a WHERE a.accountID=:id")
    Account findByAccountIDWithProfiles(int id);

    // Thêm phần mới từ remote
    @Query("SELECT a FROM Account a WHERE a.profile.profileID = :profileId")
    Account findByProfile_ProfileID(@Param("profileId") Integer profileId);
}
