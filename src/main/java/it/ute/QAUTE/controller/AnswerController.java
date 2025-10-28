package it.ute.QAUTE.controller;

import it.ute.QAUTE.api.FastApiClient;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Answer;
import it.ute.QAUTE.entity.Consultant;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.entity.Question.QuestionStatus;
import it.ute.QAUTE.exception.AppException;
import it.ute.QAUTE.exception.ErrorCode;
import it.ute.QAUTE.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/consultant")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private FastApiClient fastApiClient;

    @PostMapping("/questions/answer")
    public String handlePostAnswer(
            @RequestParam("questionId") Integer questionId,
            @RequestParam("content") String content,
            @RequestParam(value = "confirmed", defaultValue = "false") boolean confirmed,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/auth/login";
        }
        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);

        if (account.getRole() != Account.Role.Consultant) {
             redirectAttributes.addFlashAttribute("error","true");
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có tư vấn viên mới có thể trả lời.");
            redirectAttributes.addAttribute("highlightQuestionId", questionId);
            return "redirect:/consultant/questions";
        }

        if (!confirmed) { 
            int toxicResult = 0;
            try {
                toxicResult = fastApiClient.predictToxic(content);
                System.out.println("Result: toxic" + toxicResult);
            } catch (Exception e) {
                System.err.println("Lỗi khi gọi API kiểm tra toxic: " + e.getMessage());
                 redirectAttributes.addFlashAttribute("error","true");
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không thể kiểm tra nội dung câu trả lời. Vui lòng thử lại sau.");
            }

            if (toxicResult == 1) {
                redirectAttributes.addFlashAttribute("confirmToxic", true); 
                redirectAttributes.addFlashAttribute("toxicContent", content); 
                redirectAttributes.addFlashAttribute("toxicQuestionId", questionId); 
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Nội dung câu trả lời có thể không phù hợp. Vui lòng xác nhận để gửi hoặc sửa lại.");
                redirectAttributes.addAttribute("highlightQuestionId", questionId); 
                return "redirect:/consultant/questions"; 
            }
        }

        Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Question question = questionService.findById(questionId);
        if (question == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Câu hỏi không tồn tại.");
            return "redirect:/consultant/questions";
        }

        saveAnswerAndNotify(question, consultant, content, account);
        String successMsg = confirmed ? "Câu trả lời của bạn đã được gửi (sau khi xác nhận)."
                : "Câu trả lời của bạn đã được gửi thành công!";
        
        redirectAttributes.addFlashAttribute("success","true");
        redirectAttributes.addFlashAttribute("successMessage", successMsg);
        redirectAttributes.addAttribute("highlightQuestionId", questionId); // Highlight
        return "redirect:/consultant/questions";
    }

    private void saveAnswerAndNotify(Question question, Consultant consultant, String content, Account senderAccount) {
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setConsultant(consultant);
        answer.setContent(content);
        answer.setDateAnswered(LocalDateTime.now());
        answerService.saveAnswer(answer);

        question.setStatus(QuestionStatus.Answered);
        questionService.saveQuestion(question);

        try {
            if (question.getUser() != null && question.getUser().getProfile() != null
                    && question.getUser().getProfile().getAccount() != null) {
                String notificationTitle = "Câu hỏi của bạn đã được trả lời";
                String notificationContent = String.format(
                        "Câu hỏi '%s' của bạn đã được tư vấn viên %s trả lời.",
                        question.getTitle() != null && !question.getTitle().isBlank() ? question.getTitle()
                                : "không có tiêu đề",
                        senderAccount.getProfile().getFullName());
                notificationService.createNotificationForSpecificUser(
                        senderAccount, question.getUser().getProfile().getAccount(), notificationTitle,
                        notificationContent, false);
            } else {
                System.err
                        .println("Không gửi thông báo: Thiếu User/Account người hỏi. QID: " + question.getQuestionID());
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo QID " + question.getQuestionID() + ": " + e.getMessage());
        }
    }

}