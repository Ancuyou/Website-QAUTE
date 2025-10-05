package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

// này dùng cho thêm sửa xóa liên quan tài khoản
@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AuthenticationService authenticationService;
    public Account findUserByEmail(String email){
        return accountRepository.findByEmail(email);
    }
    public void changePassword(String email,String password){
        Account account=accountRepository.findByEmail(email);
        account.setPassword(authenticationService.hashed(password));
        accountRepository.save(account);
    }
    public void createAccount(String username,String password,String email){
        Account account=new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPassword(authenticationService.hashed(password));
        account.setRole(Account.Role.User);
        account.setCreatedDate(new Date());
        Profiles profiles=new Profiles();
        profiles.setFullName("user"+(1000 + new Random().nextInt(9000)));
        account.setProfile(profiles);
        accountRepository.save(account);
    }
}
