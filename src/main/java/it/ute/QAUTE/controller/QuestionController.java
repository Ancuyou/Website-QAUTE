package it.ute.QAUTE.controller;

import it.ute.QAUTE.entity.*;
import it.ute.QAUTE.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private AnswerService answerService;

    @Autowired
    private ConsultantService consultantService;

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
    @PostMapping("/questions/answer")
    public String handlePostAnswer(@RequestParam("questionId") Integer questionId,
                                   @RequestParam("content") String content,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);

        // Ensure the user is a consultant
        if (account.getRole() != Account.Role.Consultant) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có tư vấn viên mới có thể trả lời.");
            return "redirect:/consultant/questions";
        }

        Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin tư vấn viên."));

        Answer answer = new Answer();
        Question question = new Question();
        question.setQuestionID(questionId);

        answer.setQuestion(question);
        answer.setConsultant(consultant);
        answer.setContent(content);
        answer.setDateAnswered(LocalDateTime.now());

        answerService.saveAnswer(answer);

        redirectAttributes.addFlashAttribute("successMessage", "Câu trả lời của bạn đã được gửi thành công!");
        return "redirect:/consultant/questions";
    }
}