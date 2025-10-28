package it.ute.QAUTE.service;

import it.ute.QAUTE.dto.MessageDTO;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Messages;
import it.ute.QAUTE.entity.Profiles;

import java.util.List;

public interface MessageService {
    Messages saveMessage(MessageDTO messageDTO);

    List<Messages> getChatHistory(Integer senderId, Integer receiverId);

    List<Messages> getRecentChats(Integer profileId);

    Account.Role getRole(Integer id);

    void createMessage(Integer userId, Integer consultantId);

    void updateConversation(Integer messageId, String status);

    List<Profiles> getAllChatUsers(int profileID);

    Messages findById(Long messageId);

    Messages save(Messages message);
}
