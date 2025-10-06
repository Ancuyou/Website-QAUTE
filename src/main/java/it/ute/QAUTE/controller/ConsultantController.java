package it.ute.QAUTE.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/consultant")
public class ConsultantController {
    @GetMapping({"", "/"})
    public String consultantRoot() {
        return "redirect:/consultant/home";
    }
    
    @GetMapping("/home")
    public String homeConsultant() {
        return "pages/consultant/home";
    }

    @GetMapping("/profile")
    public String profileConsultant() {
        return "pages/consultant/profile";
    }
    
    @GetMapping("/questions")
    public String questionsConsultant() {
        return "pages/consultant/questions";
    }
    
    @GetMapping("/chats")
    public String chatsConsultant() {
        return "pages/consultant/chats";
    }
    
    @GetMapping("/history")
    public String historyConsultant() {
        return "pages/consultant/history";
    }
}
