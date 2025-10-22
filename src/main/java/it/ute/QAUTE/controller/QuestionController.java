package it.ute.QAUTE.controller;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.QuestionService;
import it.ute.QAUTE.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/user")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @GetMapping("/questions")
    public String showQuestionPage(Model model, Principal principal) {
        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);

        model.addAttribute("account", account);
        model.addAttribute("questions", questionService.getAllQuestions());
        model.addAttribute("departments", questionService.getAllDepartments());
        model.addAttribute("fields", questionService.getAllFields());
        return "pages/user/questions";
    }

    @PostMapping("/questions/ask")
    public String handleAskQuestion(@ModelAttribute Question question, @RequestParam("file") MultipartFile file,
                                     Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);
        // Using orElseThrow for better error handling
        User user = userService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new RuntimeException("User not found for profile ID: " + account.getProfile().getProfileID()));

        question.setUser(user);
        question.setDateSend(LocalDateTime.now());
        question.setStatus(Question.QuestionStatus.Pending);
        
       
        
        if (file != null && !file.isEmpty()) {
            String acceptfiles = "image/png,image/jpeg,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation";
            if (!file.isEmpty() && !acceptfiles.contains(file.getContentType())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Loại tệp không được hỗ trợ. Vui lòng chọn ảnh hoặc tài liệu.");
                return "redirect:/user/questions";
            }
            String uploadDir = "src/main/resources/static/images/questions/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = "Question_" + account.getAccountID() + extension;
            String uuid = java.util.UUID.randomUUID().toString().substring(0, 5);
            fileName = uuid + "_" + fileName;
            question.setFileAttachment("/images/questions/" + fileName);
            java.nio.file.Path filePath = Paths.get(uploadDir, fileName);
            try {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gửi câu hỏi: " + e.getMessage());
                e.printStackTrace();
            }
        }
        questionService.saveQuestion(question);

        redirectAttributes.addFlashAttribute("successMessage", "Câu hỏi của bạn đã được gửi thành công!");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        username = principal.getName();
        var a = accountService.findUserByUsername(username);
        if(a.getRole().equals("Consultant")){
            return "redirect:/consultant/questions-answer";
        }
        return "redirect:/user/questions";
    }
}