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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String handleAskQuestion(@ModelAttribute Question question, Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);
        // Using orElseThrow for better error handling
        User user = userService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new RuntimeException("User not found for profile ID: " + account.getProfile().getProfileID()));

        question.setUser(user);
        question.setDateSend(LocalDateTime.now());
        question.setStatus("Pending");

        questionService.saveQuestion(question);

        redirectAttributes.addFlashAttribute("successMessage", "Câu hỏi của bạn đã được gửi thành công!");
        return "redirect:/user/questions";
    }
}