package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.dto.MessageDTO;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Conversation;
import it.ute.QAUTE.entity.Messages;
import it.ute.QAUTE.entity.Messages.MessageType;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.repository.ConversationRepository;
import it.ute.QAUTE.repository.MessageRepository;
import it.ute.QAUTE.repository.ProfilesRepository;
import it.ute.QAUTE.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageServiceImplement implements MessageService {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ProfilesRepository profilesRepository;
    @Autowired
    private ConversationRepository conversationRepository;
    
    public Messages saveMessage(MessageDTO messageDTO) {
        Messages message = new Messages();
        message.setSenderID(messageDTO.getSenderID());
        message.setReceiverID(messageDTO.getReceiverID());
        message.setContent(messageDTO.getContent());
        
        try {
            if (messageDTO.getType().equals("TEXT")) {
                message.setType(MessageType.text);
            } else {
                message.setType(MessageType.valueOf(messageDTO.getType()));
            }
        } catch (IllegalArgumentException e) {
            message.setType(MessageType.text);
        }
        
        message.setStatus(messageDTO.getStatus());
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        
        return messageRepository.save(message);
    }
    
    public List<Messages> getChatHistory(Integer senderId, Integer receiverId) {
        return messageRepository.findChatHistory(senderId, receiverId);
    }

    public List<Messages> getRecentChats(Integer profileId) {
        return messageRepository.findRecentChats(profileId);
    }
    public Account.Role getRole(Integer id){
        Profiles profiles=profilesRepository.findById(id).get();
        return profiles.getAccount().getRole();
    }
    public void createMessage(Integer userId, Integer consultantId){
        if(!conversationRepository.exitsConversationByUserIdAndConsultantId(Long.valueOf(userId), Long.valueOf(consultantId))) {
            Conversation conversation = new Conversation();
            conversation.setUserProfileId(Math.toIntExact(userId));
            conversation.setConsultantProfileId(consultantId);
            conversationRepository.save(conversation);
        }
    }
    public void updateConversation(Integer messageId,String status) {
        if ("unsatisfied".equalsIgnoreCase(status)) {
            Optional<Messages> message = messageRepository.findById(Long.valueOf(messageId));
            Conversation conversation = conversationRepository.findByUserIdAndConsultantId(Long.valueOf(message.get().getReceiverID()), Long.valueOf(message.get().getSenderID()));
            conversation.setAiEnabled(false);
            conversationRepository.save(conversation);
        }
    }
    public List<Profiles> getAllChatUsers(int profileID) {
        System.out.println("Fetching chat users for profile ID: " + profileID);
       List<Messages> recentChats = getRecentChats(profileID);
       Map<Integer, Profiles> userMap = new HashMap<>();
       for (Messages message : recentChats) {
           userMap.put(message.getSenderID(), message.getSender());
           userMap.put(message.getReceiverID(), message.getReceiver());
       }
        userMap.remove(profileID);
        List<Profiles> result = userMap.values().stream().collect(Collectors.toList());
        return result;
    }

    public Messages findById(Long messageId) {
        return messageRepository.findById(messageId).orElse(null);
    }

    public Messages save(Messages message) {
        return messageRepository.save(message);
    }

}