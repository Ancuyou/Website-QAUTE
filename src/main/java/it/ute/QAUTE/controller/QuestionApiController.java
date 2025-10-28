package it.ute.QAUTE.controller;

import it.ute.QAUTE.dto.DepartmentDTO;
import it.ute.QAUTE.dto.FieldDTO;
import it.ute.QAUTE.dto.QuestionDetailDTO;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Field;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.exception.AppException;
import it.ute.QAUTE.exception.ErrorCode;
import it.ute.QAUTE.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionApiController {

    private final QuestionLikeService questionLikeService;
    private final AccountService accountService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final QuestionService questionService;

    @PostMapping("/{questionId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Integer questionId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Account account = accountService.findByUsername(authentication.getName());
        if (account == null || account.getProfile() == null) {
            return ResponseEntity.status(401).build();
        }
        // Lấy User từ Profile
        User user = userService.findByProfileId(account.getProfile().getProfileID()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(403).build();
        }
        try {
            // Toggle like và lấy trạng thái mới
            boolean liked = questionLikeService.toggleLike(questionId, user);
            // Lấy số lượt like mới nhất SAU KHI toggle
            long likeCount = questionLikeService.getLikeCount(questionId); // Cần thêm hàm này
            Map<String, Object> response = new HashMap<>();
            response.put("liked", liked);
            response.put("likeCount", likeCount); // **Thêm dòng này**
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Optional: Bắt lỗi nếu questionId không tồn tại
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{questionId}/view")
    public ResponseEntity<Void> incrementView(@PathVariable Integer questionId) {
        questionLikeService.incrementViews(questionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/fields/{departmentId}")
    @ResponseBody
    public java.util.List<FieldDTO> getFieldsByDepartment(@PathVariable Integer departmentId) {
        java.util.List<Field> fields = questionService.getFieldsByDepartmentId(departmentId);
        return fields.stream()
                .map(f -> new FieldDTO(f.getFieldID(), f.getFieldName()))
                .toList();
    }

    @GetMapping("/fields/all")
    @ResponseBody
    public java.util.List<FieldDTO> getAllFields() {
        return questionService.getAllFields().stream()
                .map(f -> new FieldDTO(f.getFieldID(), f.getFieldName()))
                .toList();
    }

    // Endpoint mới để lấy chi tiết câu hỏi cho modal Edit
    @GetMapping("/details/{id}")
    @ResponseBody
    public ResponseEntity<QuestionDetailDTO> getQuestionDetailsForEdit(@PathVariable Integer id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = principal.getName();
        Account account = accountService.findUserByUsername(username);
        User user = userService.findByProfileId(account.getProfile().getProfileID())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        try {
            Question question = questionService.findQuestionByIdForEditing(id, user.getUserID()); // Service method mới
            if (question == null) {
                return ResponseEntity.notFound().build();
            }

            // Chuyển đổi sang DTO để gửi về client
            QuestionDetailDTO dto = new QuestionDetailDTO();
            dto.setQuestionID(question.getQuestionID());
            dto.setTitle(question.getTitle());
            dto.setContent(question.getContent());
            dto.setFileAttachment(question.getFileAttachment());
            dto.setCanEdit(question.getAnswers().isEmpty() && question.getLikes() == 0 && question.getStatus() == Question.QuestionStatus.Pending); // Logic kiểm tra quyền sửa
            if (question.getDepartment() != null) {
                dto.setDepartment(new DepartmentDTO(question.getDepartment().getDepartmentID(), question.getDepartment().getDepartmentName()));
            }
            if (question.getField() != null) {
                dto.setField(new FieldDTO(question.getField().getFieldID(), question.getField().getFieldName()));
            }
            return ResponseEntity.ok(dto);
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // API để lấy danh sách Departments (cần cho modal edit)
    @GetMapping("/departments/all")
    @ResponseBody
    public List<DepartmentDTO> getAllDepartments() {
        return departmentService.findAll().stream()
                .map(d -> new DepartmentDTO(d.getDepartmentID(), d.getDepartmentName()))
                .collect(Collectors.toList());
    }
}

