package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@Service
public class AdminService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    public Page<Account> getPage(int page, int size,String keyword,String role) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (!keyword.isEmpty() && !role.isEmpty()) {
            Account.Role roleEnum = Account.Role.valueOf(role);
            return accountRepository.searchByKeywordAndRole(keyword,roleEnum,pageable);
        }else if (!keyword.isEmpty()) {
            return accountRepository.searchByKeywordExceptAdmin(keyword,pageable);
        }else if (!role.isEmpty()) {
            Account.Role roleEnum = Account.Role.valueOf(role);
            return accountRepository.searchByRole(roleEnum,pageable);
        }
        return accountRepository.findAllWithProfile(pageable);
    }
    public Account findById(Integer id) {
        return accountRepository.findByAccountIDWithProfiles(id);
    }
}
