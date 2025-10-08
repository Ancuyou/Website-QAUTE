package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Messages, Long> {
    
    // Sử dụng CreatedAt thay vì SentAt
    @Query(value = "SELECT * FROM Messages m WHERE " +
           "(m.SenderID = :senderID AND m.ReceiverID = :receiverID) OR " +
           "(m.SenderID = :receiverID AND m.ReceiverID = :senderID) " +
           "ORDER BY m.CreatedAt ASC", nativeQuery = true)
    List<Messages> findChatHistory(@Param("senderID") Integer senderID, @Param("receiverID") Integer receiverID);

    @Query(value = "SELECT * FROM Messages m WHERE " +
           "m.SenderID = :profileID OR m.ReceiverID = :profileID " +
           "ORDER BY m.CreatedAt DESC", nativeQuery = true)
    List<Messages> findRecentChats(@Param("profileID") Integer profileID);
}