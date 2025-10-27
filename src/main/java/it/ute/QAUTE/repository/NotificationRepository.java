package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer> {
    @Query("SELECT n FROM Notification n " +
            "JOIN FETCH n.sender s " +
            "JOIN FETCH s.profile " +
            "WHERE s.accountID=:accountId")
    Page<Notification> findNotificationsBySenderId(long accountId,
                                                   Pageable pageable);
    Notification findByNotificationID(Long id);

    @Query("""
    SELECT n FROM Notification n
    JOIN FETCH n.sender s
    JOIN FETCH s.profile
    WHERE s.accountID = :accountId
      AND (:q IS NULL OR :q = '' OR LOWER(n.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :q, '%')))
      AND (:status IS NULL OR :status = '' OR n.status = :status)
    """)
    Page<Notification> searchNotificationsBySenderId(@Param("q") String q,
                                                     @Param("status") String status,
                                                     @Param("accountId") long accountId,
                                                     Pageable pageable);
}
