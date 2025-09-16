package it.ute.QAUTE.controller;

import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "pages/login";
    }

    @PostMapping("/login")
    public String authLogin(@ModelAttribute("user") User user,
                            jakarta.servlet.http.HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        try {
            var auth = authenticationService.authentication(user);
            if(auth.isAuthenticated()) {
                // create token to save cookie
                org.springframework.http.ResponseCookie cookie =
                        org.springframework.http.ResponseCookie.from("ACCESS_TOKEN", auth.getToken())
                                .httpOnly(true)
                                .secure(true)
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(java.time.Duration.ofMinutes(60))
                                .build();
                response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

                return "redirect:/home";
            } else {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
                redirectAttributes.addFlashAttribute("user", user);
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi trong quá trình đăng nhập");
            redirectAttributes.addFlashAttribute("user", user);
            log.info(e.getMessage());
            return "redirect:/login";
        }
    }
}