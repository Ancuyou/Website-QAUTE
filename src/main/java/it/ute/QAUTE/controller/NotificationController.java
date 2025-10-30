package it.ute.QAUTE.controller;

import com.nimbusds.jose.JOSEException;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Notification;
import it.ute.QAUTE.entity.NotificationReceiver;
import it.ute.QAUTE.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private AccountService accountService;
    @GetMapping("/user")
    public String getNotificationsByAccount(HttpSession session, Model model, HttpServletRequest request, HttpServletResponse response)
            throws ParseException, JOSEException {
        Object tokenObj = session.getAttribute("ACCESS_TOKEN");
        Account account = authenticationService.getCurrentAccount();
        List<NotificationReceiver> notifications = notificationService.findNotificationByAccountId(account.getAccountID());
        long unreadCount = notifications.stream()
                .filter(n -> !n.isRead())
                .count();
        System.out.println("Avatar " + account.getProfile().getAvatar());
        System.out.println("account.getRole(): " + account.getRole());
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("accountId", account.getAccountID());
        System.out.println("accountId: "+account.getAccountID());
        return "fragments/userDropDown :: notificationItems";
    }
    @GetMapping("/detail")
    public String notificationDetail(
            @RequestParam("receiverId") Long receiverId,
            Model model
    ) {
        Notification notification=notificationService.findNotificationByNotificationReceiverId(receiverId);
        model.addAttribute("notification", notification);
        return "fragments/notificationModal :: modal";
    }
}
