package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.repository.ProfilesRepository;
import it.ute.QAUTE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfilesRepository profilesRepository;
    public void changePassword(String email,String password){
        Account account=accountRepository.findByEmail(email);
        account.setPassword(authenticationService.hashed(password));
        accountRepository.save(account);
    }
    public Account findById(int id){
        return accountRepository.findByAccountID(id);
    }
    public void updateAccount(Account account){
        accountRepository.save(account);
    }
    public void createAccount(String username, String password, String email) {
        Profiles profiles = new Profiles();
        profiles.setFullName("user" + (1000 + new Random().nextInt(9000)));
        profiles.setPhone("0000000000");
        profiles.setAvatar(null);
        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPassword(authenticationService.hashed(password));
        account.setRole(Account.Role.User);
        account.setCreatedDate(new Date());
        account.setProfile(profiles);
        User user = new User();
        user.setStudentCode("123");
        user.setProfile(profiles);
        user.setRoleName("Sinh Viên");
        profiles.setUser(user);
        profiles.setAccount(account);
        accountRepository.save(account);
    }

    public Account findUserByUsername(String username){
        return accountRepository.findByUsername(username);
    }
    
    public Profiles getProfileByUsername(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account != null) {
            return account.getProfile();
        }
        return null;
    }

    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
    public Page<Account> searchByKeywordAndRole(String search, Account.Role role, Pageable pageable){
        return accountRepository.searchByKeywordAndRole(search, role, pageable);
    }
    public Page<Account> findAccountByRole(Account.Role role, Pageable pageable){
        if (role == Account.Role.User) {
            return accountRepository.findAccountByUser(role, pageable);
        }
        return accountRepository.getListAccount(role, pageable);
    }
    public Account insertAccount(Account account){
        account.setCreatedDate(new Date());
        return accountRepository.save(account);
    }
    public void blockOrOpenAccount(Integer id){
        Account acc = accountRepository.findByAccountID(id);
        if (acc.isBlock()){
            acc.setBlock(Boolean.FALSE);
            accountRepository.save(acc);
        }
        else {
            acc.setBlock(Boolean.TRUE);
            accountRepository.save(acc);
        }
    }
}
