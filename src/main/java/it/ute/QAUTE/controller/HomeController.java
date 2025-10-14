package it.ute.QAUTE.controller;

import it.ute.QAUTE.dto.ConsultantDTO;
import it.ute.QAUTE.entity.*;
import it.ute.QAUTE.repository.AnswerRepository;
import it.ute.QAUTE.repository.QuestionRepository;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.ConsultantService;
import it.ute.QAUTE.service.MessageService;
import it.ute.QAUTE.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConsultantService consultantService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;

    @GetMapping("/user/home")
    public String homeUser(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Account account = accountService.findUserByUsername(username);
            User user = userService.findByProfileId(account.getProfile().getProfileID())
                    .orElse(null);
            model.addAttribute("account", account);
            if (user != null){
                long questionsAsked = questionRepository.countByUser(user);
                long answersReceived = answerRepository.countByQuestionUser(user);
                long consultantsChatted = messageService.getRecentChats(account.getProfile().getProfileID())
                        .stream()
                        .map(m -> m.getSenderID().equals(account.getProfile().getProfileID()) ? m.getReceiverID() : m.getSenderID())
                        .distinct()
                        .count();
                Map<String, Long> userStats = new HashMap<>();
                userStats.put("questionsAsked", questionsAsked);
                userStats.put("answersReceived", answersReceived);
                userStats.put("consultantsChatted", consultantsChatted);
                model.addAttribute("userStats", userStats);
            }
            // Lấy top 3 câu hoi của người dùng để demo
            List<Question> recentQuestions = questionRepository.findTop3ByUserOrderByDateSendDesc(user);
            model.addAttribute("recentActivities", recentQuestions);
            // Lấy các câu hỏi mới nhất trong cộng đồng
            List<Question> communityQuestions = questionRepository.findTop5ByOrderByDateSendDesc();
            model.addAttribute("communityQuestions", communityQuestions);
            List<ConsultantDTO> consultants = consultantService.getAllConsultants();
            model.addAttribute("consultants", consultants);
            List<Profiles> chatConsultants = messageService.getAllChatUsers(account.getProfile().getProfileID());
            List<ConsultantDTO> chatConsultantDTOs = chatConsultants.stream()
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
                    .toList();
            model.addAttribute("chatConsultants", chatConsultantDTOs);
            if (account.getProfile() != null) {
                List<Messages> recentChats = messageService.getRecentChats(account.getProfile().getProfileID());
                model.addAttribute("recentChats", recentChats);
            }
        }
        return "pages/user/home";
    }

    @GetMapping("/home/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        System.out.println("username = " + username);
        Account account = accountService.findUserByUsername(username);
        User user = userService.findByProfileId(account.getProfile().getProfileID())
                .orElse(null);
        if (user == null) {
            user = new User();
            user.setProfile(account.getProfile());
        }
        model.addAttribute("account", account);
        model.addAttribute("user", user);
        return "pages/profile";
    }
}
