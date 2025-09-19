package it.ute.QAUTE.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.repository.UserReponsitory;
import it.ute.QAUTE.service.AuthenticationService;
import it.ute.QAUTE.service.EmailService;
import it.ute.QAUTE.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.util.Map;

@Slf4j
@Controller
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    private String otp;
    @GetMapping("/auth/login")
    public String loginForm(Model model, HttpServletRequest request, HttpServletResponse response) {
        final String COOKIE_PATH = "/";
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("ACCESS_TOKEN".equals(c.getName())) {
                    String token = c.getValue();
                    if (token != null && !token.isBlank()) {
                        try {
                            SignedJWT jwt = authenticationService.verifyToken(token);
                            if (jwt != null) return "redirect:/home";
                        } catch (Exception ex) {
                            // token hỏng -> xóa cookie, KHÔNG throw AppException ở đây
                            ResponseCookie delete = ResponseCookie.from("ACCESS_TOKEN", "")
                                    .httpOnly(true).secure(false).sameSite("Lax")
                                    .path(COOKIE_PATH).maxAge(0).build();
                            response.addHeader(HttpHeaders.SET_COOKIE, delete.toString());
                        }
                    }
                }
            }
        }

        model.addAttribute("user", new User());
        return "pages/login";
    }

    @PostMapping("/auth/login")
    public String authLogin(@ModelAttribute("user") User user,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        try {
            var auth = authenticationService.authentication(user);
            if (auth.isAuthenticated()) {
                // set cookie với path "/"
                ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", auth.getToken())
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(java.time.Duration.ofMinutes(60))
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                return "redirect:/home";
            } else {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
                redirectAttributes.addFlashAttribute("user", user);
                return "redirect:/auth/login";
            }
        } catch (Exception e) {
            log.warn("Login error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi trong quá trình đăng nhập");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/auth/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        final String COOKIE_PATH = "/";
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(c.getName())) {
                    String token = c.getValue();
                    try {
                        if (token != null && !token.isBlank()) authenticationService.logout(token);
                    } catch (Exception ignored) {}
                }
            }
        }
        ResponseCookie delete = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true).secure(false).sameSite("Lax").path(COOKIE_PATH).maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, delete.toString());
        return "redirect:/auth/login";
    }
    @GetMapping("/auth/forgotPassword")
    public String forgotPasswordForm(Model model){
        model.addAttribute("showEmailForm", true);
        model.addAttribute("showOtpForm", false);
        model.addAttribute("showResetForm", false);
        return  "pages/forgotPassword";
    }
    @PostMapping("/auth/forgotPassword")
    public String forgotPassword(@RequestParam("email") String email,Model model){
        System.out.println(email);
        if(email!=null && email.endsWith("@student.hcmute.edu.vn") ){
            /*if (userReponsitory.existsByEmail(email)) {
                model.addAttribute("email", email);
                model.addAttribute("showOtpForm", true);
            }else {
                model.addAttribute("error", "Email không khớp với tài khoản nào vui lòng nhập lại");
                model.addAttribute("showResetForm", false);
            }*/
            model.addAttribute("email", email);
            model.addAttribute("showEmailForm", false);
            model.addAttribute("showOtpForm", true);
            model.addAttribute("showResetForm", false);
            otp=emailService.sendEmailOTP(email);
        }else {
            model.addAttribute("error", "Email không hợp lệ vui lòng nhập lại");
            model.addAttribute("showResetForm", false);
            model.addAttribute("showEmailForm", true);
            model.addAttribute("showOtpForm", false);
        }
        return "pages/forgotPassword";
    }
    @PostMapping("/auth/verifyOtp")
    public String verifyOTP(@RequestParam Map<String, String> params,Model model){
        String inputOTP = params.get("otp1") + params.get("otp2") + params.get("otp3") + params.get("otp4") + params.get("otp5") + params.get("otp6");
        if(inputOTP!=null && inputOTP.equals(otp)){
            model.addAttribute("showEmailForm", false);
            model.addAttribute("showOtpForm", false);
            model.addAttribute("showResetForm", true);
            model.addAttribute("email", params.get("email"));
        }else {
            model.addAttribute("error", "OTP không đúng hoặc đã hết hạn");
            model.addAttribute("showOtpForm", true);
            model.addAttribute("showResetForm", false);
            model.addAttribute("showEmailForm", false);
            model.addAttribute("email", params.get("email"));
        }
        return "pages/forgotPassword";
    }
    @PostMapping("/auth/resetPassword")
    public String resetPassword(@RequestParam Map<String, String> params,Model model){
        String newPassword = params.get("newPassword");
        String confirmPassword = params.get("confirmPassword");
        if (newPassword.equals(confirmPassword)) {
            System.out.println("cập nhật mật khẩu mới");
            return "redirect:/auth/login";
        }else {
            model.addAttribute("showEmailForm", false);
            model.addAttribute("showOtpForm", false);
            model.addAttribute("showResetForm", true);
            model.addAttribute("email", params.get("email"));
            return "pages/forgotPassword";
        }
    }
}
