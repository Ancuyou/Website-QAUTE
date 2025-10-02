package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    public boolean existsByEmail(String email){
        return accountRepository.existsByEmail(email);
    }
    public boolean existsByUsername(String username){return accountRepository.existsByUsername(username);}
    public Account findUserByEmail(String email){
        return accountRepository.findByEmail(email);
    }
    public void saveAccount(Account account){
        accountRepository.save(account);
    }
}
