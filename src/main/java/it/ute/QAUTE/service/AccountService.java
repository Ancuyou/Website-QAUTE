package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService {
    void changePassword(String email, String password);

    Account findById(int id);

    void updateAccount(Account account);

    void createAccount(String username, String password, String email);

    Account findUserByUsername(String username);


    Account findByUsername(String username);

    Page<Account> searchByKeywordAndRole(String search, Account.Role role, Pageable pageable);

    Page<Account> searchUserByKeywordAndRoleName(String search, Pageable pageable);

    Page<Account> findAccountByRoleAndUserRole(User.Role roleName, Pageable pageable);

    // ??? wtf
    Page<Account> findAccountByRole(Account.Role role, Pageable pageable);


    Account blockOrOpenAccount(Integer id);

    Account findAccountByID(Integer id);

    @Transactional
    Account createManagerOrConsultant(Account account, String password, MultipartFile avatarFile);

    @Transactional
    Account editManagerOrConsultant(Account account, String pass, MultipartFile avatarFile);

    void deleteAccount(Integer id);

    void save(Account account);

    void updateAccountOffline(Integer id);

    String isAccountOnline(Integer id);


    long countAll_User();
}
