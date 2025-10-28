package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.entity.User;

import java.util.Map;
import java.util.Optional;

public interface UserService {
    Optional<User> findByProfileId(Integer profileId);

    Profiles getCurrentUserProfile(String username);

    Profiles getProfileById(Integer profileId);

    Map<String,String> mapRole();
}
