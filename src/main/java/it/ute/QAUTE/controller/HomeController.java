package it.ute.QAUTE.controller;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {
    @Autowired
    private AccountService  accountService;
    @Autowired
    private UserService userService;
    @GetMapping("/user/home")
    public String homeUser() {
        return "pages/user/home";
    }

    @GetMapping("/consultant/home")
    public String homeConsultant() {
        return "pages/consultant/home";
    }
    @GetMapping("/home/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        System.out.println("username = " + username);
        Account account = accountService.findUserByUsername(username);
        User user = userService.findByProfileId(account.getProfile().getProfileID())
                .orElse(null);
        if (user == null) {
            user = new User();
            user.setProfile(account.getProfile());
            user.setRoleName("Sinh viên");
        }
        model.addAttribute("account", account);
        model.addAttribute("user", user);
        return "pages/profile";
    }

}
