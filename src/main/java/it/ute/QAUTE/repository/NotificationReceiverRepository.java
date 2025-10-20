package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.NotificationReceiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationReceiverRepository extends JpaRepository<NotificationReceiver, Integer> {
}
