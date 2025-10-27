package it.ute.QAUTE.service.Implement;

import com.github.benmanes.caffeine.cache.Cache;
import it.ute.QAUTE.api.FastApiClient;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Conversation;
import it.ute.QAUTE.entity.Messages;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.repository.ConversationRepository;
import it.ute.QAUTE.repository.ProfilesRepository;
import it.ute.QAUTE.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AIServiceImplement implements AIService {
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private MessageServiceImplement messageService;
    @Autowired
    private ProfilesRepository  profilesRepository;
    @Autowired
    private Cache<Integer, Boolean> onlineCache;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private FastApiClient fastApiClient;
    @Async("aiExecutor")
    public void replyMessage(Messages userMsg){
        if (userMsg.getType() != Messages.MessageType.text) return;
        if (Boolean.TRUE.equals(userMsg.getAi())) return;
        Account.Role fromRole = messageService.getRole(userMsg.getSenderID());
        Account.Role toRole   = messageService.getRole(userMsg.getReceiverID());
        if (fromRole != Account.Role.User || toRole != Account.Role.Consultant) return;
        Integer userId=userMsg.getSenderID();
        Integer consultantId=userMsg.getReceiverID();
        Optional<Profiles> consultantProfiles=profilesRepository.findById(consultantId);
        Boolean status = onlineCache.getIfPresent(consultantProfiles.get().getAccount().getAccountID());
        if(status!=null) return;
        Conversation conversation=conversationRepository.findByUserIdAndConsultantId(Long.valueOf(userId), Long.valueOf(consultantId));
        if(conversation.getAiEnabled() && !conversation.getConsultantJoined()){
            String text=aiReply(userMsg.getContent());
            Messages message = new Messages();
            message.setSenderID(consultantId);
            message.setReceiverID(userId);
            message.setContent(text);
            message.setType(Messages.MessageType.text);
            message.setAi(true);
            message.setCreatedAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());
            Messages saved = messageService.save(message);
            String sCh = "/topic/chat/" + userId + "/" + consultantId;
            String rCh = "/topic/chat/" + consultantId + "/" + userId;
            messagingTemplate.convertAndSend(sCh, saved);
            messagingTemplate.convertAndSend(rCh, saved);
        }
    }
    public String aiReply(String text){
        System.out.println("gọi AI");
        return fastApiClient.chatBlocking(text, java.time.Duration.ofSeconds(30));
    }
}
