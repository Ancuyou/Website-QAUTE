package it.ute.QAUTE.service;

import it.ute.QAUTE.repository.UserReponsitory;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserReponsitory userReponsitory;
    public boolean existsByEmail(String email){
        return userReponsitory.existsByEmail(email);
    }
}
