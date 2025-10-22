package it.ute.QAUTE.controller;


import it.ute.QAUTE.entity.Department;
import it.ute.QAUTE.entity.Field;
import it.ute.QAUTE.entity.Question;
import it.ute.QAUTE.repository.DepartmentRepository;
import it.ute.QAUTE.repository.FieldRepository;
import it.ute.QAUTE.repository.QuestionRepository;
import it.ute.QAUTE.service.DepartmentService;
import it.ute.QAUTE.service.FieldService;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Autowired
    private FieldService fieldService;

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

    @PostMapping("/questions/update/{id}")
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

    @GetMapping("/fields")
    public String listFields(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fieldID").descending());
        Page<Field> fieldPage = fieldService.searchField(departmentId, keyword, pageable);

        List<Department> departments = departmentService.findAll();

        model.addAttribute("fields", fieldPage.getContent());
        model.addAttribute("departments", departments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", fieldPage.getTotalPages());
        model.addAttribute("totalElements", fieldPage.getTotalElements());
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", "fields");


        return "pages/manager/fields";
    }

    @GetMapping("/fields/new")
    public String newField(Model model) {
        model.addAttribute("field", new Field());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("active", "fields");
        return "pages/manager/addField";
    }

    @PostMapping("/fields/save")
    public String saveField(
            @ModelAttribute("field") Field field,
            @RequestParam(value = "departmentIds", required = false) List<Integer> departmentIds,
            RedirectAttributes redirectAttributes) {

        try {
            if (departmentIds != null && !departmentIds.isEmpty()) {
                Set<Department> selectedDepartments = new HashSet<>(departmentService.findAllById(departmentIds));
                field.setDepartments(selectedDepartments);
            } else {
                field.setDepartments(new HashSet<>());
            }

            fieldRepository.save(field);

            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm lĩnh vực thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm lĩnh vực thất bại: " + e.getMessage());
        }

        return "redirect:/manager/fields";
    }

    @GetMapping("/fields/edit/{id}")
    public String editField(@PathVariable Integer id, Model model) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lĩnh vực ID: " + id));

        model.addAttribute("field", field);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("active", "fields");

        return "pages/manager/editField";
    }

    @PostMapping("/fields/update/{id}")
    public String updateField(
            @PathVariable Integer id,
            @ModelAttribute("field") Field field,
            @RequestParam(value = "departmentIds", required = false) List<Integer> departmentIds,
            RedirectAttributes redirectAttributes) {

        try {
            Field existing = fieldRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lĩnh vực ID: " + id));

            existing.setFieldName(field.getFieldName());

            if (departmentIds != null && !departmentIds.isEmpty()) {
                Set<Department> selectedDepartments = new HashSet<>(departmentService.findAllById(departmentIds));
                existing.setDepartments(selectedDepartments);
            } else {
                existing.setDepartments(new HashSet<>());
            }

            fieldRepository.save(existing);

            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lĩnh vực thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/manager/fields";
    }



    @ResponseBody
    @GetMapping("/fields/by-department/{departmentId}")
    public List<Field> getFieldsByDepartment(@PathVariable Integer departmentId) {
        return fieldRepository.findAllByDepartments_departmentID(departmentId);  // speed run
    }




}
