package it.ute.QAUTE.controller;

import it.ute.QAUTE.dto.ConsultantDTO;
import it.ute.QAUTE.dto.MessageDTO;
import it.ute.QAUTE.dto.UserDTO;
import it.ute.QAUTE.entity.Consultant;
import it.ute.QAUTE.entity.Messages;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle WebSocket messages sent to /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDTO messageDTO) {
        // Save message using the service
        Messages savedMessage = messageService.saveMessage(messageDTO);
        
        // Broadcast to both sender and receiver channels
        String senderChannel = "/topic/chat/" + messageDTO.getSenderID() + "/" + messageDTO.getReceiverID();
        String receiverChannel = "/topic/chat/" + messageDTO.getReceiverID() + "/" + messageDTO.getSenderID();
        
        messagingTemplate.convertAndSend(senderChannel, savedMessage);
        messagingTemplate.convertAndSend(receiverChannel, savedMessage);
    }

    /**
     * API to get chat history between two users
     */
    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<Messages> getChatHistory(
            @RequestParam("senderId") Integer senderId,
            @RequestParam("receiverId") Integer receiverId) {
        return messageService.getChatHistory(senderId, receiverId);
    }
    
    /**
     * API for consultants to get list of users they've chatted with
     */
    @GetMapping("/api/chat/users")
    @ResponseBody
    public List<UserDTO> getChatUsers(@RequestParam("profileId") Integer profileId) {
        System.out.println("API /api/chat/users called with profileId: " + profileId);
        List<Profiles> chatUsers = messageService.getAllChatUsers(profileId);
        System.out.println("Found " + chatUsers.size() + " profiles in chat history");
        for (Profiles profile : chatUsers) {
            User user = profile.getUser();
            System.out.println("Profile ID: " + profile.getProfileID() + ", Full Name: " + profile.getFullName() + ", User: " + (user != null ? "exists" : "null"));
        }
        
        List<UserDTO> results = chatUsers.stream()
            .map(profile -> {
                User user = profile.getUser();
                System.out.println("Processing profile: " + profile.getProfileID() + ", fullName: " + profile.getFullName() + ", user: " + (user != null ? "found" : "null"));
                if(user == null) {
                    return null; 
                }
                UserDTO dto = new UserDTO();
                dto.setUserID(user.getUserID());
                dto.setProfileID(profile.getProfileID());
                dto.setFullName(profile.getFullName());
                dto.setAvatar(profile.getAvatar());
                dto.setIsOnline(false);
                return dto;
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
        
        System.out.println("Returning " + results.size() + " users after filtering");
        return results;
    }
    
    /**
     * API for users to get list of consultants they've chatted with
     */
    @GetMapping("/api/chat/consultants")
    @ResponseBody
    public List<ConsultantDTO> getChatConsultants(@RequestParam("profileId") Integer profileId) {
        List<Profiles> chatConsultants = messageService.getAllChatUsers(profileId);
        
        return chatConsultants.stream()
            .map(profile -> {
                Consultant consultant = profile.getConsultant();
                if (consultant != null) {
                    ConsultantDTO dto = new ConsultantDTO();
                    dto.setConsultantID(consultant.getConsultantID());
                    dto.setProfileID(profile.getProfileID());
                    dto.setFullName(profile.getFullName());
                    dto.setAvatar(profile.getAvatar());
                    dto.setExperienceYears(consultant.getExperienceYears());
                    dto.setIsOnline(false);
                    return dto;
                }
                return null;
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }
}