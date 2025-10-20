package it.ute.QAUTE.controller;

import it.ute.QAUTE.dto.AnswerQuestionDTO;
import it.ute.QAUTE.dto.UserDTO;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Answer;
import it.ute.QAUTE.entity.Consultant;
import it.ute.QAUTE.entity.Messages;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.AnswerService;
import it.ute.QAUTE.service.ConsultantService;
import it.ute.QAUTE.service.MessageService;
import it.ute.QAUTE.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/consultant")
public class ConsultantController {
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private AccountService accountService;

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private QuestionService questionService;
 
    @Autowired
    private AnswerService answerService;
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
    public String profileConsultant(Model model, Principal principal) {
        String username = principal.getName();
        System.out.println("username = " + username);
        Account account = accountService.findUserByUsername(username);
        Profiles profile = account.getProfile();
        Consultant consultant = profile.getConsultant();
        model.addAttribute("account", account);
        model.addAttribute("profile", profile);
        model.addAttribute("consultant", consultant);

        return "pages/consultant/profile";
    }
    @PostMapping("/profile/update")
    public String updateProfile(
        @RequestParam("fullName") String fullName,
        @RequestParam("phone") String phone,
        @RequestParam(value = "experienceYears", required = false) Integer experienceYears,
        @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) throws IOException {

        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);
        Profiles profile = account.getProfile();
        Consultant consultant = profile.getConsultant();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String uploadDir = "src/main/resources/static/images/avatars/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();
            String originalFileName = avatarFile.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = "Consultant_" + account.getAccountID() + extension;
            profile.setAvatar("/images/avatars/" + fileName);
            java.nio.file.Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        profile.setFullName(fullName);
        profile.setPhone(phone);
        if (consultant != null && experienceYears != null) {
            consultant.setExperienceYears(experienceYears);
            consultantService.updateConsultant(consultant);
        }
        accountService.updateAccount(account);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/consultant/profile";
    }

    @GetMapping("/questions")
    public String questionsConsultant(Principal principal, 
                                     Model model,
                                     @RequestParam(required = false) Integer highlightQuestion) {
        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);

        model.addAttribute("account", account);
        model.addAttribute("questions", questionService.getAllQuestions());
        model.addAttribute("departments", questionService.getAllDepartments());
        model.addAttribute("fields", questionService.getAllFields());
        
        // Truyền ID câu hỏi cần highlight
        if (highlightQuestion != null) {
            model.addAttribute("highlightQuestion", highlightQuestion);
        }
        
        return "pages/consultant/questions-answer";
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
    public String historyConsultant(
            Model model,
            Principal principal,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer timeRange,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);
        Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin tư vấn viên."));

        // Gọi service lấy Page<Answer>
        Page<Answer> answerPage = answerService.getAnswersHistoryByConsultant(
                consultant.getConsultantID(), timeRange, keyword, PageRequest.of(page, size));

        List<AnswerQuestionDTO> answerQuestionDTOs = answerPage.getContent().stream()
            .map(answer -> {
                AnswerQuestionDTO dto = new AnswerQuestionDTO();
                dto.setAnswerID(answer.getAnswerID());
                dto.setConsultantID(consultant.getConsultantID());
                dto.setConsultantName(account.getProfile().getFullName());
                dto.setContentAnswer(answer.getContent());
                dto.setAnswerAt(answer.getDateAnswered());

                if (answer.getQuestion() != null) {
                    dto.setQuestionID(answer.getQuestion().getQuestionID());
                    dto.setTitle(answer.getQuestion().getTitle());
                    dto.setCreatedAt(answer.getQuestion().getDateSend());
                    dto.setContentQuestion(answer.getQuestion().getContent());

                    if (answer.getQuestion().getUser() != null &&
                        answer.getQuestion().getUser().getProfile() != null) {
                        dto.setUserName(answer.getQuestion().getUser().getProfile().getFullName());
                        dto.setUserID(answer.getQuestion().getUser().getUserID());
                    }
                }
                return dto;
            })
            .toList();

        long uniqueQuestions = answerQuestionDTOs.stream()
            .map(AnswerQuestionDTO::getQuestionID)
            .distinct()
            .count();
        long uniqueUsers = answerQuestionDTOs.stream()
            .map(AnswerQuestionDTO::getUserID)
            .filter(id -> id != null)
            .distinct()
            .count();

        model.addAttribute("account", account);
        model.addAttribute("answersHistory", answerQuestionDTOs);
        model.addAttribute("totalAnswers", answerPage.getTotalElements());
        model.addAttribute("uniqueQuestions", uniqueQuestions);
        model.addAttribute("uniqueUsers", uniqueUsers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", answerPage.getTotalPages());
        model.addAttribute("pageSize", size);

        return "pages/consultant/history";
    }
    @GetMapping("/question-details/{id}")
    public String questionDetails(@PathVariable("id") Integer questionId, Model model, Principal principal) {
        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);

        var question = questionService.getQuestionById(questionId);
        var answers = answerService.getAnswersByQuestionId(questionId);

        model.addAttribute("account", account);
        model.addAttribute("question", question);
        model.addAttribute("answers", answers);

        return "pages/consultant/questiondetails";
    }
}
