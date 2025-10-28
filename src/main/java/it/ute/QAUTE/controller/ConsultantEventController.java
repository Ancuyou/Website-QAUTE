package it.ute.QAUTE.controller;

import it.ute.QAUTE.exception.AppException;
import it.ute.QAUTE.exception.ErrorCode;
import it.ute.QAUTE.api.FastApiClient;
import it.ute.QAUTE.entity.*;
import it.ute.QAUTE.service.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/consultant/events")
public class ConsultantEventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private FastApiClient fastApiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("")
    public String listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            Model model,
            Principal principal) {
        // 1. Lấy thông tin consultant (Phần này đã đúng)
        Account account = accountService.findUserByUsername(principal.getName());
        Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 2. Sửa logic lọc (Filter)
        Page<Event> events;

        boolean isStatusFilter = status != null && !status.isEmpty();
        boolean isTypeFilter = type != null && !type.isEmpty();

        if (isStatusFilter || isTypeFilter) {
            Event.EventStatus eventStatus = isStatusFilter ? Event.EventStatus.valueOf(status) : null;
            Event.EventType eventType = isTypeFilter ? Event.EventType.valueOf(type) : null;

            events = eventService.findEventsByConsultantAndFilters(consultant, eventType, eventStatus, pageable);
        } else {
            events = eventService.findEventsByConsultant(consultant, pageable);
        }

        long totalEvents = eventService.countConsultantEvents(consultant);
        long pendingEvents = eventService.countConsultantEventsByStatus(consultant, Event.EventStatus.Pending);

        List<Event.EventStatus> approvedStatuses = Arrays.asList(
                Event.EventStatus.Approved,
                Event.EventStatus.Ongoing,
                Event.EventStatus.Completed);
        long approvedEvents = eventService.countConsultantEventsByStatusIn(consultant, approvedStatuses);

        model.addAttribute("account", account);
        model.addAttribute("events", events.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", events.getTotalPages());
        model.addAttribute("totalItems", events.getTotalElements()); // Tổng số item sau khi lọc
        model.addAttribute("totalEvents", totalEvents); // Tổng sự kiện (all-time)
        model.addAttribute("pendingEvents", pendingEvents); // Tổng chờ duyệt (all-time)
        model.addAttribute("approvedEvents", approvedEvents); // Tổng đã duyệt (all-time)
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("eventTypes", Arrays.asList(Event.EventType.values()));
        model.addAttribute("eventStatuses", Arrays.asList(Event.EventStatus.values()));

        return "pages/consultant/events/list";
    }

    @GetMapping("/new")
    public String createEventForm(Model model, Principal principal) {
        Account account = accountService.findUserByUsername(principal.getName());

        List<Field> fields = fieldService.getAllFields();
        model.addAttribute("fields", fields);
        model.addAttribute("account", account);
        model.addAttribute("event", new Event());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("eventTypes", Arrays.asList(Event.EventType.values()));
        model.addAttribute("eventModes", Arrays.asList(Event.EventMode.values()));

        return "pages/consultant/events/create";
    }

    @PostMapping("/create")
    public String createEvent(
            @Valid @ModelAttribute Event event,
            BindingResult bindingResult, 
            @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer fieldId,
            Principal principal,
            RedirectAttributes ra,
            Model model) {
        if (event.getStartTime() != null && event.getEndTime() != null
                && event.getEndTime().isBefore(event.getStartTime())) {
            bindingResult.rejectValue("endTime", "error.event", "Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("fields", fieldService.getAllFields());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("eventTypes", Arrays.asList(Event.EventType.values()));
            model.addAttribute("eventModes", Arrays.asList(Event.EventMode.values()));
            model.addAttribute("account", accountService.findUserByUsername(principal.getName()));
            return "pages/consultant/events/create";
        }
        try {
            Account account = accountService.findUserByUsername(principal.getName());
            Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

            event.setConsultant(consultant);

            if (departmentId != null) {
                event.setDepartment(departmentService.findById(departmentId));
            }

            if (fieldId != null) {
                event.setField(fieldService.getFieldById(fieldId));
            }

            eventService.createEvent(event, bannerFile);
            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("successMessage",
                    "Tạo sự kiện thành công! Sự kiện đang chờ phê duyệt từ Manager.");
            return "redirect:/consultant/events";
        } catch (AppException e) {
            model.addAttribute("errorMessage", "Tạo sự kiện thất bại: " + e.getMessage());

            model.addAttribute("fields", fieldService.getAllFields());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("eventTypes", Arrays.asList(Event.EventType.values()));
            model.addAttribute("eventModes", Arrays.asList(Event.EventMode.values()));
            model.addAttribute("account", accountService.findUserByUsername(principal.getName()));

            return "pages/consultant/events/create"; 
        }
    }

    @GetMapping("/details/{id}")
    public String eventDetails(
            @PathVariable Integer id,
            Model model,
            Principal principal) {
        Account account = accountService.findUserByUsername(principal.getName());
        Event event = eventService.findById(id);

        Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!(event.getConsultant().getConsultantID() == consultant.getConsultantID())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<EventRegistration> participants = eventService.findEventParticipants(id);

        model.addAttribute("account", account);
        model.addAttribute("event", event);
        model.addAttribute("participants", participants);
        model.addAttribute("canEdit", event.getStatus() == Event.EventStatus.Pending ||
                event.getStatus() == Event.EventStatus.Approved);
        model.addAttribute("canCancel", event.getStatus() != Event.EventStatus.Completed &&
                event.getStatus() != Event.EventStatus.Cancelled);

        return "pages/consultant/events/details";
    }

    @GetMapping("/edit/{id}")
    public String editEventForm(
            @PathVariable Integer id,
            Model model,
            Principal principal) {
        Account account = accountService.findUserByUsername(principal.getName());
        Event event = eventService.findById(id);

        // Check ownership
        Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!(event.getConsultant().getConsultantID() == consultant.getConsultantID())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (event.getStatus() == Event.EventStatus.Completed ||
                event.getStatus() == Event.EventStatus.Cancelled) {
            throw new AppException(ErrorCode.INVALID_EVENT_TIME);
        }

        List<Field> fields = fieldService.getAllFields();

        model.addAttribute("account", account);
        model.addAttribute("event", event);
        model.addAttribute("fields", fields);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("eventTypes", Arrays.asList(Event.EventType.values()));
        model.addAttribute("eventModes", Arrays.asList(Event.EventMode.values()));

        return "pages/consultant/events/edit";
    }

    @PostMapping("/update/{id}")
    public String updateEvent(
            @PathVariable Integer id,
            @ModelAttribute Event event,
            @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer fieldId,
            Principal principal,
            RedirectAttributes ra) {
        try {
            if (departmentId != null) {
                event.setDepartment(departmentService.findById(departmentId));
            }

            if (fieldId != null) {
                event.setField(fieldService.getFieldById(fieldId));
            }

            eventService.updateEvent(id, event, bannerFile);

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("successMessage", "Cập nhật sự kiện thành công!");

            return "redirect:/consultant/events/details/" + id;

        } catch (AppException e) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
            return "redirect:/consultant/events/edit/" + id;
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelEvent(
            @PathVariable Integer id,
            @RequestParam String reason,
            Principal principal,
            RedirectAttributes ra) {
        try {
            Account account = accountService.findUserByUsername(principal.getName());
            Event event = eventService.findById(id);

            Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

            if (!(event.getConsultant().getConsultantID() == consultant.getConsultantID())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            eventService.cancelEvent(id, reason);

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("successMessage", "Đã hủy sự kiện thành công!");

        } catch (AppException e) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("errorMessage", "Hủy sự kiện thất bại: " + e.getMessage());
        }

        return "redirect:/consultant/events";
    }

    @PostMapping("/delete/{id}")
    public String deleteEvent(
            @PathVariable Integer id,
            Principal principal,
            RedirectAttributes ra) {
        try {
            Account account = accountService.findUserByUsername(principal.getName());
            Event event = eventService.findById(id);

            // Check ownership
            Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

            if (!(event.getConsultant().getConsultantID() == consultant.getConsultantID())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            eventService.deleteEvent(id);

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("successMessage", "Đã xóa sự kiện thành công!");

        } catch (AppException e) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("errorMessage", "Xóa sự kiện thất bại: " + e.getMessage());
        }

        return "redirect:/consultant/events";
    }

    @GetMapping("/participants/{id}")
    public String viewParticipants(
            @PathVariable Integer id,
            Model model,
            Principal principal) {
        Account account = accountService.findUserByUsername(principal.getName());
        Event event = eventService.findById(id);

        // Check ownership
        Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!(event.getConsultant().getConsultantID() == consultant.getConsultantID())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        var participants = eventService.findEventParticipants(id);

        model.addAttribute("account", account);
        model.addAttribute("event", event);
        model.addAttribute("participants", participants);
        long confirmedCount = participants.stream()
                .filter(p -> "Confirmed".equals(p.getStatus().toString())
                        || "Attended".equals(p.getStatus().toString()))
                .count();
        model.addAttribute("confirmedCount", confirmedCount);

        long attendedCount = participants.stream()
                .filter(p -> "Attended".equals(p.getStatus().toString()))
                .count();
        model.addAttribute("attendedCount", attendedCount);

        long absentCount = participants.stream()
                .filter(p -> "Absent".equals(p.getStatus().toString()))
                .count();
        model.addAttribute("absentCount", absentCount);

        return "pages/consultant/events/participants";
    }

    @PostMapping("/participants/update-status/{eventId}")
    public String updateParticipantStatus(
            @PathVariable Integer eventId,
            @RequestParam Integer registrationId,
            @RequestParam String status,
            Principal principal,
            RedirectAttributes ra) {
        try {
            Account account = accountService.findUserByUsername(principal.getName());
            Consultant consultant = consultantService.findByProfileId(account.getProfile().getProfileID())
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
            EventRegistration.RegistrationStatus newStatus = EventRegistration.RegistrationStatus.valueOf(status);

            eventService.updateRegistrationStatus(registrationId, newStatus, consultant);

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ: " + status);
        } catch (AppException e) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/consultant/events/participants/" + eventId;
    }

   @PostMapping("/suggest-from-text")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suggestEventDetails(@RequestBody String eventText) {
        if (eventText == null || eventText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nội dung không được để trống"));
        }

        String aiJsonResponse = null;
        String jsonToParse = null;

        try {
  
            String prompt = "Trích xuất Tiêu đề (title), Mô tả (description), " +
                    "Thời gian bắt đầu (startTime - định dạng YYYY-MM-DDTHH:MM), " +
                    "Thời gian kết thúc (endTime - định dạng YYYY-MM-DDTHH:MM), " +
                    "Địa điểm (location), " +
                    "Hình thức (mode - Online, Offline, hoặc Hybrid), " +
                    "Tên Khoa (departmentName - ví dụ: 'Công nghệ thông tin'), " + 
                    "Tên Lĩnh vực (fieldName - ví dụ: 'Học thuật') " + 
                    "từ mô tả sự kiện sau. " +
                    "Chỉ trả về một đối tượng JSON duy nhất chứa các trường này. " +
                    "Nếu không tìm thấy thông tin nào, để giá trị là null.\n\nMô tả sự kiện:\n\"" +
                    eventText + "\"\n\nJSON Output:";

           
            aiJsonResponse = fastApiClient.chatBlocking(prompt, java.time.Duration.ofSeconds(60)); // Tăng timeout nếu cần
            log.info("AI Response (Raw): {}", aiJsonResponse);

            if (aiJsonResponse.contains("Xin lỗi, hệ thống đang bận")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", aiJsonResponse));
            }

            Pattern jsonPattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(aiJsonResponse);

            if (matcher.find()) {
                jsonToParse = matcher.group(1).trim();
                log.info("Extracted JSON from ```json block: {}", jsonToParse);
            } else {
                int firstBrace = aiJsonResponse.indexOf('{');
                int lastBrace = aiJsonResponse.lastIndexOf('}');
                if (firstBrace != -1 && lastBrace > firstBrace) {
                    jsonToParse = aiJsonResponse.substring(firstBrace, lastBrace + 1).trim();
                    log.warn("Could not find ```json block. Fallback to first {{...}} block: {}", jsonToParse);
                } else {
                    throw new JsonProcessingException("AI response does not contain a valid JSON block."){};
                }
            }
            Map<String, Object> suggestions = objectMapper.readValue(jsonToParse, Map.class);
            return ResponseEntity.ok(suggestions);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse extracted JSON. Error: {}. Raw AI Response: [{}]. Extracted Part: [{}]", 
                      e.getMessage(), aiJsonResponse, jsonToParse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Lỗi xử lý phản hồi từ AI. Phản hồi không phải JSON hợp lệ.", 
                "rawResponse", aiJsonResponse
            ));
        } catch (Exception e) {
            log.error("Cannot suggest event details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Không thể gợi ý thông tin từ AI: " + e.getMessage()));
        }
    }
}