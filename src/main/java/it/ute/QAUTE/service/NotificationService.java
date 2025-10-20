package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Notification;
import it.ute.QAUTE.entity.NotificationReceiver;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.repository.NotificationReceiverRepository;
import it.ute.QAUTE.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private NotificationReceiverRepository notificationReceiverRepository;
    public Page<Notification> findAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }
    public Notification findNotificationById(Integer id) {
        return notificationRepository.findById(id).orElse(null);
    }
    public void updateNotification(String title, String content, String targetType,String status) {

    }
    public void createNotification(Account sender, String title, String content, String targetType,String status){
        Notification notification = new Notification();
        notification.setContent(content);
        notification.setSender(sender);
        notification.setTitle(title);
        if ("ALL".equalsIgnoreCase(targetType)) {
            notification.setTargetType(Account.Role.User);
        } else {
            try {
                notification.setTargetType(Account.Role.valueOf(targetType));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("❌ Loại đối tượng không hợp lệ: " + targetType);
            }
        }
        notification.setStatus(status);
        notification.setCreatedDate(new Date());
        Notification savedNotification = notificationRepository.save(notification);
        sendByRole(savedNotification,targetType,status);
    }
    public void sendByRole(Notification savedNotification,String targetType,String status){
        if ("PUBLISHED".equalsIgnoreCase(status)) {
            List<Account> receivers;
            if ("ALL".equalsIgnoreCase(targetType)) {
                receivers=accountRepository.findAllExcludeAdmin();
            }else {
                receivers=accountRepository.findByRoleExcludeAdmin(Account.Role.valueOf(targetType));
            }
            for (Account receiver : receivers) {
                NotificationReceiver  notificationReceiver = new NotificationReceiver();
                notificationReceiver.setReceiver(receiver);
                notificationReceiver.setRead(false);
                notificationReceiver.setNotification(savedNotification);
                notificationReceiverRepository.save(notificationReceiver);
            }
            System.out.println("✅ Gửi thông báo tới " + receivers.size() + " người dùng.");
        }else {
            System.out.println("lưu nháp");
        }
    }
}
