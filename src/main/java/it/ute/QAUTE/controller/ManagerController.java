package it.ute.QAUTE.controller;


import it.ute.QAUTE.entity.Department;
import it.ute.QAUTE.entity.Field;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.repository.DepartmentRepository;
import it.ute.QAUTE.repository.FieldRepository;
import it.ute.QAUTE.repository.QuestionRepository;
import it.ute.QAUTE.service.DepartmentService;
import it.ute.QAUTE.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/manager")
public class ManagerController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private DepartmentService  departmentService;

    @GetMapping("/questions")
    public String listQuestions(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) Integer departmentId,
                                @RequestParam(required = false) Integer fieldId,
                                @RequestParam(required = false) String userName,
                                @RequestParam(required = false) String status,
                                Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("dateSend").descending());
        Page<Question> questionPage = questionService.filterQuestions(departmentId, fieldId, userName, status, pageable);  // this username is Ho va ten not acc

        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questionPage.getTotalPages());
        model.addAttribute("totalElements", questionPage.getTotalElements());

        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("fields", fieldRepository.findAllByDepartments_departmentID(departmentId));

        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedFieldId", fieldId);
        model.addAttribute("userName", userName);
        model.addAttribute("selectedStatus", status);

        return "pages/manager/questions";
    }

    @GetMapping("/questions/edit/{id}")
    public String editQuestionForm(@PathVariable Integer id, Model model) {
        Question question = questionService.findById(id);

        if (question == null) {
            return "redirect:/manager/questions";
        }

        List<Department> departments = departmentService.findAll();
        List<Field> fields = fieldRepository.findAll();

        model.addAttribute("question", question);
        model.addAttribute("departments", departments);
        model.addAttribute("fields", fields);

        return "pages/manager/editQuestion";
    }

    @PostMapping("questions/update/{id}")
    public String updateQuestion(
            @PathVariable Integer id,
            @ModelAttribute Question question,
            RedirectAttributes redirectAttributes) {

        try {
            Question existingQuestion = questionService.findById(id);

            if (existingQuestion == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy câu hỏi!");
                return "redirect:/manager/questions";
            }

            existingQuestion.setTitle(question.getTitle());
            existingQuestion.setContent(question.getContent());
            existingQuestion.setStatus(question.getStatus());
            existingQuestion.setDepartment(question.getDepartment());
            existingQuestion.setField(question.getField());

            questionService.save(existingQuestion);

            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Sửa thành công! Question ");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Sửa thất bại: " + e.getMessage());
        }

        return "redirect:/manager/questions";
    }

    @ResponseBody
    @GetMapping("/fields/by-department/{departmentId}")
    public List<Field> getFieldsByDepartment(@PathVariable Integer departmentId) {
        return fieldRepository.findAllByDepartments_departmentID(departmentId);  // speed run
    }
}
