package it.ute.QAUTE.controller;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.AdminService;
import jakarta.persistence.criteria.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private AccountService accountService;
    @GetMapping("/home")
    public String home(@RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "search", defaultValue = "") String search,
                       @RequestParam(name = "role", defaultValue = "") String role,
                       @RequestParam(name = "section", defaultValue = "users") String section,
                       Model model) {
        Page<Account> pageData = adminService.getPage(page, size,search,role);
        model.addAttribute("accounts", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("pageSizeOptions", new int[]{5, 10, 20, 50});
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("section", section);
        return "pages/admin/home";
    }
    @GetMapping("/users/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Account account=adminService.findById(id);
        model.addAttribute("account",account);
        return "pages/admin/edit";
    }
    @PostMapping("users/update")
    public String update(@ModelAttribute Account account,
                         @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile) throws IOException {
        System.out.println(account.getAccountID());
        Account existing = adminService.findById(account.getAccountID());
        if (existing == null) {
            throw new RuntimeException("Account not found!");
        }
        existing.setEmail(account.getEmail());
        existing.setRole(account.getRole());
        existing.getProfile().setFullName(account.getProfile() != null ? account.getProfile().getFullName() : existing.getProfile().getFullName());
        existing.getProfile().setPhone(account.getProfile() != null ? account.getProfile().getPhone() : existing.getProfile().getPhone());
        if (avatarFile!=null) {
            String uploadDir = "src/main/resources/static/images/avatars/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();
            String originalFileName = avatarFile.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = account.getAccountID() + extension;
            existing.getProfile().setAvatar("/images/avatars/" + fileName);
            java.nio.file.Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        accountService.updateAccount(existing);
        return "redirect:/admin/home?section=users";
    }
}
