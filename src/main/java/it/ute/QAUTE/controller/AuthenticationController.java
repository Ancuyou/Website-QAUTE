package it.ute.QAUTE.controller;

import com.nimbusds.jwt.SignedJWT;
import it.ute.QAUTE.configuration.CustomJwtDecoder;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.AuthenticationService;
import it.ute.QAUTE.service.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
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

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Slf4j
@Controller
@NoArgsConstructor
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private EmailService emailService;
    //Post
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
                            var role = customJwtDecoder.decode(token).getClaims().get("scope");
                            if (jwt != null){
                                switch (role.toString()) {
                                    case "ROLE_User" -> {
                                        return "redirect:/user/home";
                                    }
                                    case "ROLE_Consultant" -> {
                                        return "redirect:/consultant/home";
                                    }
                                    default -> {
                                        return "redirect:/auth/login";
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ResponseCookie delete = ResponseCookie.from("ACCESS_TOKEN", "")
                                    .httpOnly(true).secure(false).sameSite("Lax")
                                    .path(COOKIE_PATH).maxAge(0).build();
                            response.addHeader(HttpHeaders.SET_COOKIE, delete.toString());
                        }
                    }
                }
            }
        }
        // khúc này tạo phân quyền
        model.addAttribute("account", new Account());
        return "pages/login";
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
        return  "pages/forgotPassword";
    }
    @GetMapping("/auth/register")
    public String registerForm() {
        return "pages/register";
    }
    // Post
    @Autowired
    private CustomJwtDecoder customJwtDecoder;
    @PostMapping("/auth/login")
    public String authLogin(@ModelAttribute("account") Account account,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        try {
            var auth = authenticationService.authentication(account);
            if (auth.isAuthenticated()) {
                ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", auth.getToken())
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofHours(1))
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                switch (account.getRole()) {
                    case User -> {
                        return "redirect:/user/home";
                    }
                    case Consultant -> {
                        return "redirect:/consultant/home";
                    }
                    default -> {
                        redirectAttributes.addFlashAttribute("error", "Người dùng không có quyền truy cập vào trang web");
                        redirectAttributes.addFlashAttribute("account", account);
                        return "redirect:/auth/login";
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
                redirectAttributes.addFlashAttribute("account", account);
                return "redirect:/auth/login";
            }
        } catch (Exception e) {
            log.warn("Login error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi trong quá trình đăng nhập");
            redirectAttributes.addFlashAttribute("account", account);
            return "redirect:/auth/login";
        }
    }
    @PostMapping("/auth/forgotPassword")
    public String forgotPassword(@RequestParam("email") String email,Model model,HttpSession session){
        System.out.println(email);
        if(email!=null && email.endsWith("@student.hcmute.edu.vn") ){
            if (accountService.existsByEmail(email)) {
                String otp=emailService.sendForgetPasswordEmail(email);
                session.setAttribute("otp", authenticationService.hashed(otp));
                session.setAttribute("otpExpiry", System.currentTimeMillis() + (3 * 60 * 1000));
                model.addAttribute("email", email);
                model.addAttribute("showOtpForm", true);
            }else {
                model.addAttribute("error", "Email không khớp với tài khoản nào vui lòng nhập lại");
                model.addAttribute("showEmailForm", true);
            }
        }else {
            model.addAttribute("error", "Email không hợp lệ vui lòng nhập lại");
            model.addAttribute("showEmailForm", true);
        }
        return "pages/forgotPassword";
    }

    @PostMapping("/auth/verifyOtp")
    public String verifyOTP(@RequestParam Map<String, String> params, Model model, HttpSession session){
        String inputOTP = params.get("otp1") + params.get("otp2") + params.get("otp3") + params.get("otp4") + params.get("otp5") + params.get("otp6");
        Integer failCount=(Integer) session.getAttribute("failCount");
        String hashedOtp= session.getAttribute("otp").toString();
        Long otpExpiry = (Long) session.getAttribute("otpExpiry");
        if (failCount==null) failCount=0;
        if (otpExpiry==null||otpExpiry<System.currentTimeMillis() ) {
            model.addAttribute("error", "OTP đã hết hạn");
            model.addAttribute("showOtpForm", true);
            model.addAttribute("email", params.get("email"));
        }else if (!authenticationService.check(inputOTP,hashedOtp)){
            failCount++;
            session.setAttribute("failCount", failCount);
            if (failCount>=3) {
                System.out.println(params.get("Tài khoản tạm thời bị khóa"));
                return "redirect:/auth/login";
            }
            model.addAttribute("error", "OTP không đúng ");
            model.addAttribute("showOtpForm", true);
            model.addAttribute("email", params.get("email"));
        } else {
            session.removeAttribute("otp");
            session.removeAttribute("otpExpiry");
            session.removeAttribute("failCount");
            model.addAttribute("showResetForm", true);
            model.addAttribute("email", params.get("email"));
        }
        return "pages/forgotPassword";
    }

    @PostMapping("/auth/resetPassword")
    public String resetPassword(@RequestParam Map<String, String> params,Model model){
        String newPassword = params.get("newPassword");
        String confirmPassword = params.get("confirmPassword");
        String email = params.get("email");
        if (newPassword.equals(confirmPassword)) {
            Account account=accountService.findUserByEmail(email);
            account.setPassword(authenticationService.hashed(newPassword));
            accountService.saveAccount(account);
            System.out.println("đổi mật khẩu thành công");
            return "redirect:/auth/login";
        }else {
            model.addAttribute("showResetForm", true);
            model.addAttribute("email", params.get("email"));
            return "pages/forgotPassword";
        }
    }
    @PostMapping("/auth/register")
    public String register(@RequestParam Map<String, String> params,Model model,HttpSession session){
        String username=params.get("username");
        String email=params.get("email");
        String password=params.get("password");
        if (accountService.existsByUsername(username) || accountService.existsByEmail(email)) {
            model.addAttribute("error", "Tài khoản đã tồn tại");
            return "pages/register";
        }
        String otp=emailService.sendRegisterEmail(email);
        System.out.println(otp);
        session.setAttribute("otp", authenticationService.hashed(otp));
        session.setAttribute("otpExpiry", System.currentTimeMillis() + (3 * 60 * 1000));
        model.addAttribute("showOtpForm", true);
        model.addAttribute("email", email);
        model.addAttribute("username", username);
        model.addAttribute("password", password);
        return "pages/register";
    }
    @PostMapping("/auth/verifyRegisterOtp")
    public String verifyRegisterOtp(@RequestParam Map<String, String> params, Model model, HttpSession session){
        String inputOTP = params.get("otp1") + params.get("otp2") + params.get("otp3") + params.get("otp4") + params.get("otp5") + params.get("otp6");
        String hashedOtp= session.getAttribute("otp").toString();
        if (authenticationService.check(inputOTP,hashedOtp)){
            session.removeAttribute("otp");
            session.removeAttribute("otpExpiry");
            String username=params.get("username");
            String email=params.get("email");
            String password=params.get("password");
            Account account=new Account();
            account.setUsername(username);
            account.setEmail(email);
            account.setPassword(authenticationService.hashed(password));
            account.setRole(Account.Role.User);
            account.setCreatedDate(new Date());
            Profiles profiles=new Profiles();
            profiles.setFullName("user"+(1000 + new Random().nextInt(9000)));
            account.setProfile(profiles);
            accountService.saveAccount(account);
            System.out.println("đăng ký thành công rồi");
        }
        return "redirect:/auth/login";
    }
    @GetMapping("/auth/google")
    public String loginGoogle(){
        System.out.println("chạy gg");
        return "redirect:/oauth2/authorization/google";
    }
}
