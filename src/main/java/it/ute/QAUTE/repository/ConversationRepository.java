package it.ute.QAUTE.repository;
import it.ute.QAUTE.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT COUNT(*)>0 FROM Conversation c WHERE c.userProfileId=:userId AND c.consultantProfileId=:consultantId")
    boolean exitsConversationByUserIdAndConsultantId(Long userId, Long consultantId);
    @Query("SELECT c FROM Conversation c WHERE c.userProfileId=:userId AND c.consultantProfileId=:consultantId")
    Conversation findByUserIdAndConsultantId(Long userId, Long consultantId);
}
