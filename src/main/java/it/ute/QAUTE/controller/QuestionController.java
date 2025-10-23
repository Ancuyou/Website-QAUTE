package it.ute.QAUTE.controller;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Answer;
import it.ute.QAUTE.entity.Consultant;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.AnswerService;
import it.ute.QAUTE.service.ConsultantService;
import it.ute.QAUTE.service.QuestionService;
import it.ute.QAUTE.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private AnswerService answerService;

    @Autowired
    private ConsultantService consultantService;

    @GetMapping("/questions")
    public String showQuestionPage(@RequestParam(required = false) Integer highlightQuestionId, Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Account account = accountService.findUserByUsername(username);
            model.addAttribute("account", account);
        }
        model.addAttribute("questions", questionService.getAllQuestions());
        model.addAttribute("departments", questionService.getAllDepartments());
        model.addAttribute("fields", questionService.getAllFields());
        if (highlightQuestionId != null) {
            model.addAttribute("highlightQuestionId", highlightQuestionId);
        }
        return "pages/user/questions";
    }

    @PostMapping("/questions/ask")
    public String handleAskQuestion(@ModelAttribute Question question,
                                    @RequestParam("file") MultipartFile file,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);
        User user = userService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new RuntimeException(
                        "User not found for profile ID: " + account.getProfile().getProfileID()
                ));

        question.setUser(user);
        question.setDateSend(LocalDateTime.now());
        question.setStatus(Question.QuestionStatus.Pending);

        if (file != null && !file.isEmpty()) {
            String acceptfiles = "image/png,image/jpeg,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation";
            if (!acceptfiles.contains(file.getContentType())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Loại tệp không được hỗ trợ. Vui lòng chọn ảnh hoặc tài liệu.");
                return "redirect:/user/questions";
            }

            String uploadDir = "src/main/resources/static/images/questions/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = "Question_" + account.getAccountID() + extension;
            String uuid = java.util.UUID.randomUUID().toString().substring(0, 5);
            fileName = uuid + "_" + fileName;

            question.setFileAttachment("/images/questions/" + fileName);

            try {
                Files.copy(file.getInputStream(), Paths.get(uploadDir, fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gửi câu hỏi: " + e.getMessage());
                return "redirect:/user/questions";
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

        if (account.getRole() != Account.Role.Consultant) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có tư vấn viên mới có thể trả lời.");
            return "redirect:/consultant/questions";
        }

        Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin tư vấn viên."));

        Answer answer = new Answer();
        Question qRef = new Question();
        qRef.setQuestionID(questionId);

        answer.setQuestion(qRef);
        answer.setConsultant(consultant);
        answer.setContent(content);
        answer.setDateAnswered(LocalDateTime.now());

        answerService.saveAnswer(answer);
        Question q = questionService.getQuestionById(questionId);
        if (q == null) {
            throw new IllegalStateException("Không tìm thấy câu hỏi.");
        }
        q.setStatus(Question.QuestionStatus.Answered);
        questionService.saveQuestion(q);

        redirectAttributes.addFlashAttribute("successMessage", "Câu trả lời của bạn đã được gửi thành công!");
        return "redirect:/consultant/questions";
    }
}
