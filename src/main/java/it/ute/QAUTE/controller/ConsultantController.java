package it.ute.QAUTE.controller;

import it.ute.QAUTE.dto.UserDTO;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Messages;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/consultant")
public class ConsultantController {
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private AccountService accountService;
    
    @GetMapping({"", "/"})
    public String consultantRoot() {
        return "redirect:/consultant/home";
    }
    
    @GetMapping("/home")
    public String homeConsultant(Model model, Authentication authentication) {
        if (authentication != null) {
            Account account = accountService.findByUsername(authentication.getName());
            List<Profiles> chatUsers = messageService.getAllChatUsers(account.getProfile().getProfileID());

            List<UserDTO> userDTOs = chatUsers.stream().map(profile -> {
                User user = profile.getUser();
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
            }).toList();

            model.addAttribute("chatUsers", userDTOs);
            model.addAttribute("account", account);

            if (account.getProfile() != null) {
                List<Messages> recentChats = messageService.getRecentChats(account.getProfile().getProfileID());
                model.addAttribute("recentChats", recentChats);
            }
        }
        return "pages/consultant/home";
    }

    @GetMapping("/profile")
    public String profileConsultant() {
        return "pages/consultant/profile";
    }
    
    @GetMapping("/questions")
    public String questionsConsultant() {
        return "pages/consultant/questions";
    }
    
    @GetMapping("/chats")
    public String chatsConsultant(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Profiles profile = accountService.getProfileByUsername(username);
        
        if (profile != null) {
            List<Messages> recentMessages = messageService.getRecentChats(profile.getProfileID());
            model.addAttribute("recentMessages", recentMessages);
        }
        
        return "pages/consultant/chats";
    }
    
    @GetMapping("/history")
    public String historyConsultant() {
        return "pages/consultant/history";
    }
    
}
