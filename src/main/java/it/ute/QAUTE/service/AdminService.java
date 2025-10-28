package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.RefreshToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AdminService {
    Account findById(Integer id);

    Page<RefreshToken> searchTokens(String keyword, Pageable pageable);

    Page<RefreshToken> findActiveTokens(String keyword, Pageable pageable);

    Page<RefreshToken> findExpiredTokens(String keyword, Pageable pageable);

    long countActiveTokens();

    long countExpiredTokens();

    long countAllTokens();

    Optional<RefreshToken> findById(String id);

    @Transactional
    void revokeToken(String id);

    @Transactional
    int deleteExpiredTokens();
}
