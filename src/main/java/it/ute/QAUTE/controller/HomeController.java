package it.ute.QAUTE.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/user/home")
    public String homeUser() {
        return "pages/user/home";
    }

    @GetMapping("/consultant/home")
    public String homeConsultant() {
        return "pages/consultant/home";
    }
    @GetMapping("/home/profile")
    public String profile() {
        return "pages/profile";
    }
}
