package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.repository.AccountRepository;
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
    public Account findUserByEmail(String email){
        return accountRepository.findByEmail(email);
    }
    public void changePassword(String email,String password){
        Account account=accountRepository.findByEmail(email);
        account.setPassword(authenticationService.hashed(password));
        accountRepository.save(account);
    }
    public void updateAccount(Account account){
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



    // search to role and keyword   (nhan so hoc code)

    public Page<Account> searchByKeywordAndRole(String search, Account.Role role, Pageable pageable){
        return accountRepository.searchByKeywordAndRole(search, role, pageable);
    }
    public Page<Account> getListAccount(Account.Role role, Pageable pageable){
        return accountRepository.getListAccount(role, pageable);
    }
    public Account insertAccount(Account account){
        account.setCreatedDate(new Date()); // fix loi khong nhan duoc gio hien tai
        return accountRepository.save(account);
    }

    public void blockOrOpenAccount(Integer id){
        Account acc = accountRepository.findByAccountID(id);
        if (acc.isBlock()){  // lock
            acc.setBlock(Boolean.FALSE);
            accountRepository.save(acc);
        }
        else {  // open
            acc.setBlock(Boolean.TRUE);
            accountRepository.save(acc);
        }

    }
}
