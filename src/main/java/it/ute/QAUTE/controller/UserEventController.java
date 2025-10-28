package it.ute.QAUTE.controller;

import it.ute.QAUTE.exception.AppException;
import it.ute.QAUTE.exception.ErrorCode;
import it.ute.QAUTE.entity.*;
import it.ute.QAUTE.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Arrays;

@Controller
@RequestMapping("/user/events")
public class UserEventController {

    private static final Logger log = LoggerFactory.getLogger(UserEventController.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;
    private <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String value) {
        if (value == null || value.isEmpty()) return false;
        try {
            Enum.valueOf(enumClass, value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @GetMapping("")
    public String listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) Integer departmentId,
            Model model,
            Principal principal) {
        Account account = accountService.findUserByUsername(principal.getName());

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());

        Page<Event> events;

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasType = type != null && !type.trim().isEmpty();
        boolean hasMode = mode != null && !mode.trim().isEmpty();
        boolean hasDept = departmentId != null;

        String effectiveType = type;
        String effectiveMode = mode;

        try {
            if (hasKeyword) {
                 events = eventService.searchEvents(keyword, pageable);
            } else if (hasType || hasMode || hasDept) {
                Event.EventType eventType = null;
                if(hasType && isValidEnum(Event.EventType.class, type)) {
                    eventType = Event.EventType.valueOf(type);
                } else if (hasType) {
                     model.addAttribute("filterError", "Loại sự kiện không hợp lệ.");
                     effectiveType = "";
                }

                Event.EventMode eventMode = null;
                 if(hasMode && isValidEnum(Event.EventMode.class, mode)) {
                    eventMode = Event.EventMode.valueOf(mode);
                 } else if (hasMode) {
                     if (!model.containsAttribute("filterError")) {
                        model.addAttribute("filterError", "Hình thức không hợp lệ.");
                     }
                    effectiveMode = "";
                 }
                
                events = eventService.filterEvents(eventType, eventMode, Event.EventStatus.Approved,
                        departmentId, null, pageable);
            } else {
                 events = eventService.findUpcomingEvents(pageable);
            }
        } catch (Exception e) { 
             log.error("Lỗi khi tải danh sách sự kiện: {}", e.getMessage());
             events = Page.empty(pageable); // Trả về trang rỗng an toàn
             model.addAttribute("filterError", "Lỗi hệ thống khi tải sự kiện.");
             effectiveType = "";
             effectiveMode = "";
             keyword = "";
        }

        model.addAttribute("account", account);
        model.addAttribute("events", events.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", events.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", effectiveType);
        model.addAttribute("selectedMode", effectiveMode);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("eventTypes", Arrays.asList(Event.EventType.values()));
        model.addAttribute("eventModes", Arrays.asList(Event.EventMode.values()));
        model.addAttribute("departments", departmentService.findAll());

        return "pages/user/events/list";
    }

    @GetMapping("/{id}")
    public String eventDetails(
            @PathVariable Integer id,
            Model model,
            Principal principal) {
        
        Account account = accountService.findUserByUsername(principal.getName());
        User user = userService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Event event = eventService.findById(id); 
        boolean isRegistered = eventService.isUserRegistered(event, user);

        boolean canRegister = event.canRegister() && !isRegistered;

        model.addAttribute("account", account);
        model.addAttribute("event", event);
        model.addAttribute("isRegistered", isRegistered);
        model.addAttribute("canRegister", canRegister);

        return "pages/user/events/details";
    }

    @PostMapping("/register/{id}")
    public String registerForEvent(
            @PathVariable Integer id,
            @RequestParam(required = false) String note,
            Principal principal,
            RedirectAttributes ra) {
        try {
            Account account = accountService.findUserByUsername(principal.getName());
            User user = userService.findByProfileId(account.getProfile().getProfileID())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            eventService.registerForEvent(id, user, note);

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("successMessage", "Đăng ký sự kiện thành công!");

            return "redirect:/user/events/" + id;

        } catch (AppException e) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("errorMessage", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/user/events/" + id;
        }
    }


    @GetMapping("/my-registrations")
    public String myRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model,
            Principal principal) {
        Account account = accountService.findUserByUsername(principal.getName());
        User user = userService.findByProfileId(account.getProfile().getProfileID())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("registeredAt").descending());

        Page<EventRegistration> registrations;
        EventRegistration.RegistrationStatus regStatus = null;
        String effectiveStatus = status;

        // SỬA LỖI LOGIC: Chuyển đổi Enum và gọi service LỌC
        if (status != null && !status.isEmpty()) {
             try {
                 regStatus = EventRegistration.RegistrationStatus.valueOf(status);
             } catch (IllegalArgumentException e) {
                 model.addAttribute("filterError", "Trạng thái lọc không hợp lệ.");
                 effectiveStatus = "";
                 // regStatus vẫn là null
             }
        }
        
        // Gọi hàm service có khả năng lọc (Giả định EventService có hàm này)
        registrations = eventService.findUserRegistrations(user, regStatus, pageable);

        model.addAttribute("account", account);
        model.addAttribute("registrations", registrations.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", registrations.getTotalPages());
        model.addAttribute("selectedStatus", effectiveStatus); // Dùng biến đã qua xử lý
        model.addAttribute("registrationStatuses",
                Arrays.asList(EventRegistration.RegistrationStatus.values()));

        return "pages/user/events/my-registrations";
    }

    @PostMapping("/registrations/{id}/cancel")
    public String cancelRegistration(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes ra) {
        try {
            Account account = accountService.findUserByUsername(principal.getName());
            User user = userService.findByProfileId(account.getProfile().getProfileID())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            eventService.cancelRegistration(id, user, reason);

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("successMessage", "Đã hủy đăng ký thành công!");

        } catch (AppException e) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("errorMessage", "Hủy đăng ký thất bại: " + e.getMessage());
        }

        return "redirect:/user/events/my-registrations";
    }

    @PostMapping("/registrations/{id}/feedback")
    public String submitFeedback(
            @PathVariable Integer id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String feedback,
            Principal principal,
            RedirectAttributes ra) {
        // Validation
        if (rating == null || rating < 1 || rating > 5) {
             ra.addFlashAttribute("error", true);
             ra.addFlashAttribute("errorMessage", "Vui lòng chọn số sao hợp lệ (1-5).");
             return "redirect:/user/events/my-registrations";
        }
        
        try {
            Account account = accountService.findUserByUsername(principal.getName());
            User user = userService.findByProfileId(account.getProfile().getProfileID())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            eventService.submitFeedback(id, user, rating, feedback);

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá!");

        } catch (Exception e) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("errorMessage", "Gửi đánh giá thất bại: " + e.getMessage());
        }

        return "redirect:/user/events/my-registrations";
    }
}