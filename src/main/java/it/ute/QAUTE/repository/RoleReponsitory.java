package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleReponsitory extends JpaRepository<Role, String> {
    Optional<Role> findByRoleName(String roleName);
}
