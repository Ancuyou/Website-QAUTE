package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.repository.UserReponsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserReponsitory userReponsitory;
    public boolean existsByEmail(String email){
        return userReponsitory.existsByEmail(email);
    }
    public void saveUser(User user){
        userReponsitory.save(user);
    }
    public User findUserByEmail(String email){
        return userReponsitory.findByEmail(email);
    }
}
