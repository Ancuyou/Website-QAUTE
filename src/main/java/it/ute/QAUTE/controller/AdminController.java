package it.ute.QAUTE.controller;

import it.ute.QAUTE.Exception.AppException;
import it.ute.QAUTE.Exception.ErrorCode;
import it.ute.QAUTE.entity.*;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.AdminService;
import it.ute.QAUTE.service.DepartmentService;
import it.ute.QAUTE.service.UserService;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private UserService userService;
    @GetMapping("/consultants")
    public String listConsultants(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("accountID").descending());
        Page<Account> data;
        if (q != null && !q.equals("")) {
            data = accountService.searchByKeywordAndRole(q, Account.Role.Consultant, pageable);
        } else {
            data = accountService.findAccountByRole(Account.Role.Consultant, pageable);
        }

        model.addAttribute("accounts", data.getContent());
        model.addAttribute("q", q);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalElements());
        model.addAttribute("pageSizeOptions", new int[]{5, 10, 15, 20});
        model.addAttribute("active", "consultants");

        return "pages/admin/consultants";
    }

    @GetMapping("/consultant/edit/{id}")
    public String editConsultant(
            @PathVariable Integer id,
            Model model
    ) {
        Account account = adminService.findById(id);
        model.addAttribute("account", account);
        return "pages/admin/editConsultant";
    }

    @GetMapping("/consultant/add")
    public String addConsultant(Model model) {
        Account acc = new Account();
        acc.setProfile(new Profiles());
        acc.getProfile().setConsultant(new Consultant());
        model.addAttribute("account", acc);
        return "pages/admin/addConsultant";
    }

    @GetMapping("/departments")
    public String listDepartments(Model model,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  @RequestParam(required = false) String q) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("departmentID").ascending());
        Page<Department> pageData = departmentService.searchNameDepartment(q, pageable);

        model.addAttribute("departments", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("q", q);

        model.addAttribute("pageSizeOptions", new int[]{5, 10, 20, 5});

        model.addAttribute("activeSection", "departments");

        return "pages/admin/departments";
    }
    @GetMapping("/department/edit/{id}")
    public String updateDepartment(
            @PathVariable Integer id,
            Model model){
        model.addAttribute("department", departmentService.findById(id)); // fix lai neu tra ve null thi bao loi he thong
        model.addAttribute("allDepartments", departmentService.findAllNoPaging());
        return "pages/admin/editDepartment";
    }

    @GetMapping("/department/insert")
    public String insertDepartment(Model model){
        model.addAttribute("department", new Department());
        model.addAttribute("allDepartments", departmentService.findAllNoPaging());
        return "pages/admin/addDepartment";
    }

    @PostMapping("/consultant/update")
    public String updateConsultant(
            @ModelAttribute("account") Account form,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile) throws IOException {

        Account existing = adminService.findById(form.getAccountID());
        if (existing == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

        existing.setUsername(form.getUsername());
        existing.setEmail(form.getEmail());
        existing.setRole(form.getRole());

        if (existing.getProfile() == null) {
            existing.setProfile(new Profiles());
        }
        if (form.getProfile() != null) {
            existing.getProfile().setFullName(form.getProfile().getFullName());
            existing.getProfile().setPhone(form.getProfile().getPhone());
        }
        existing.setPassword(form.getPassword() != null ? form.getPassword() : existing.getPassword());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String uploadDir = "src/main/resources/static/images/avatars/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            String originalFileName = avatarFile.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = form.getAccountID() + extension;
            Path filePath = Paths.get(uploadDir, fileName);

            Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            existing.getProfile().setAvatar("/images/avatars/" + fileName);
        }

        accountService.updateAccount(existing);

        return "redirect:/admin/consultants";
    }


    @PostMapping("/consultant/insert")
    public String insertConsultant(@ModelAttribute("account") Account form,
                                   @RequestParam("newPassword") String newPassword,
                                   @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile) throws IOException {
        form.setPassword(newPassword);
        form.setRole(Account.Role.Consultant);
        int AccountID = accountService.insertAccount(form).getAccountID();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String uploadDir = "src/main/resources/static/images/avatars/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            String originalFileName = avatarFile.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = AccountID + extension;
            Path filePath = Paths.get(uploadDir, fileName);

            Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            form.getProfile().setAvatar("/images/avatars/" + fileName);
        }
        // not clean, need query
        form.setAccountID(AccountID);
        accountService.updateAccount(form);

        return "redirect:/admin/consultants";
    }

    @PostMapping("/department/update")
    public String updateDepartment(@ModelAttribute Department department,
                                   @RequestParam(value = "parent.departmentID", required = false) Integer parentId) {
        if (parentId == null) {
            department.setParent(null);
        }
        departmentService.updateDepartment(department);
        return "redirect:/admin/departments";
    }

    @PostMapping("/department/insert")
    public String insertDepartment(@ModelAttribute Department department,
                                   @RequestParam(value = "parent.departmentID", required = false) Integer parentId){
        if (parentId == null) {
            department.setParent(null);
        }
        departmentService.updateDepartment(department);
        return "redirect:/admin/departments";
    }
    @PostMapping("/department/delete/{id}")
    public String deleteDepartment(@PathVariable("id") Integer id){
        departmentService.deleteDepartment(id);
        return "redirect:/admin/departments";
    }
    @GetMapping("/refresh-tokens")
    public String listRefreshTokens(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            Model model) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<RefreshToken> tokenPage;
        if (status != null && !status.isEmpty()) {
            if ("active".equals(status)) {
                tokenPage = adminService.findActiveTokens(q, pageable);
            } else if ("expired".equals(status)) {
                tokenPage = adminService.findExpiredTokens(q, pageable);
            } else {
                tokenPage = adminService.searchTokens(q, pageable);
            }
        } else {
            tokenPage = adminService.searchTokens(q, pageable);
        }

        long activeTokens = adminService.countActiveTokens();
        long expiredTokens = adminService.countExpiredTokens();
        long totalTokens = adminService.countAllTokens();

        model.addAttribute("refreshTokens", tokenPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tokenPage.getTotalPages());
        model.addAttribute("totalItems", tokenPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("pageSizeOptions", new int[]{5, 10, 15, 20});
        model.addAttribute("q", q);
        model.addAttribute("status", status);

        model.addAttribute("activeTokens", activeTokens);
        model.addAttribute("expiredTokens", expiredTokens);
        model.addAttribute("totalTokens", totalTokens);

        return "pages/admin/refreshTokens";
    }

    @GetMapping("/managers")
    public String listManagers(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("accountID").descending());
        Page<Account> data;
        if (q != null && !q.equals("")) {
            data = accountService.searchByKeywordAndRole(q, Account.Role.Manager, pageable);
        } else {
            data = accountService.findAccountByRole(Account.Role.Manager, pageable);
        }

        model.addAttribute("accounts", data.getContent());
        model.addAttribute("q", q);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalElements());
        model.addAttribute("pageSizeOptions", new int[]{5, 10, 15, 20});
        model.addAttribute("active", "managers");

        return "pages/admin/managers";
    }

    @PostMapping("/refresh-token/revoke/{id}")
    public String  revokeRefreshToken(@PathVariable("id") String id){
        adminService.revokeToken(id);
        return "redirect:/admin/refresh-tokens";
    }

    @PostMapping("/refresh-tokens/cleanup")
    public String cleanupRefreshTokens(){
        adminService.deleteExpiredTokens();
        return "redirect:/admin/refresh-tokens";
    }

    @GetMapping("/account/block/{id}")
    public String blockAccount(@PathVariable("id") Integer id) {
        accountService.blockOrOpenAccount(id);
        return "redirect:/admin/consultants"; // map ve cho manager./ not manager chua xg   }
    }
    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "") String q,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "5") int size,
                            Model model){
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("accountID").descending());
        Page<Account> data;
        if (q != null && !q.equals("")) {
            data = accountService.searchByKeywordAndRole(q, Account.Role.User, pageable);
        } else {
            data = accountService.findAccountByRole(Account.Role.User, pageable);
        }
        model.addAttribute("accounts", data.getContent());
        model.addAttribute("q", q);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalElements());
        model.addAttribute("pageSizeOptions", new int[]{5, 10, 15, 20});
        model.addAttribute("active", "users");
        return "pages/admin/users";
    }
}
