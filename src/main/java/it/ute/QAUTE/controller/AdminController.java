package it.ute.QAUTE.controller;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
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
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        System.out.println("id " + id);
        Account account=adminService.findById(id);
        model.addAttribute("account",account);
        return "pages/admin/edit";
    }
}
