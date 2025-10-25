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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    public Page<Notification> findAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }
    public Notification findNotificationByNotificationReceiverId(Long id) {
        NotificationReceiver notificationReceiver=notificationReceiverRepository.findById(id);
        if(!notificationReceiver.isRead()) {
            notificationReceiver.setRead(true);
            notificationReceiverRepository.save(notificationReceiver);
        }
        return notificationReceiver.getNotification();
    }
    public List<NotificationReceiver> findNotificationByAccountId(long receiverId){
        return notificationReceiverRepository.findByAccountId(receiverId);
    }
    public Page<Notification> findNotificationsBySenderId(long senderId,Pageable pageable){
        return notificationRepository.findNotificationsBySenderId(senderId,pageable);
    }
    public boolean deleteNotification(Long id){
        Notification notification=notificationRepository.findById(Math.toIntExact(id)).orElse(null);
        if(notification==null || notification.getStatus().equals("PUBLISHED")){
            return false;
        }
        notificationRepository.delete(notification);
        return true;
    }
    public void updateNotification(Long id,String title, String content, String targetType,String status,boolean is_priority) {
        Notification notification = notificationRepository.findByNotificationID(id);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(Notification.NotificationTarget.valueOf(targetType));
        notification.setStatus(status);
        notification.set_priority(is_priority);
        Notification savedNotification = notificationRepository.save(notification);
        Account account=accountRepository.findByAccountID(notification.getSender().getAccountID());
        sendByRole(savedNotification,targetType,status,account.getRole());
        if(status.equals("DRAFT")) deleteNotificationReceiverByNotificationId(id);
    }
    public void deleteNotificationReceiverByNotificationId(Long notificationId){
        notificationReceiverRepository.deleteAllByNotificationId(notificationId);
    }
    public void createNotification(Account sender, String title, String content, String targetType,String status,boolean is_priority){
        Notification notification = new Notification();
        notification.setContent(content);
        notification.setSender(sender);
        notification.set_priority(is_priority);
        notification.setTitle(title);
        notification.setTargetType(Notification.NotificationTarget.valueOf(targetType));
        notification.setStatus(status);
        notification.setCreatedDate(new Date());
        Notification savedNotification = notificationRepository.save(notification);
        sendByRole(savedNotification,targetType,status,sender.getRole());
    }
    public void sendByRole(Notification savedNotification,String targetType,String status,Account.Role roleSender){
        if ("PUBLISHED".equalsIgnoreCase(status)) {
            List<Account> receivers;
            if ("ALL".equalsIgnoreCase(targetType)) {
                if(roleSender.equals(Account.Role.Admin))receivers=accountRepository.findAllExcludeAdmin();
                else receivers=accountRepository.findUserAndConsultant();
            }else {
                receivers=accountRepository.findByRoleExcludeAdmin(Account.Role.valueOf(targetType));
            }
            for (Account receiver : receivers) {
                NotificationReceiver  notificationReceiver = new NotificationReceiver();
                notificationReceiver.setReceiver(receiver);
                notificationReceiver.setRead(false);
                notificationReceiver.setNotification(savedNotification);
                notificationReceiverRepository.save(notificationReceiver);
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(receiver.getUsername()),
                        "/queue/notifications",
                        savedNotification.getTitle() + ": " + savedNotification.getContent()
                );
            }
            System.out.println("✅ Gửi thông báo tới " + receivers.size() + " người dùng.");
        }else {
            System.out.println("lưu nháp");
        }
    }

    public Notification findNotificationById(Integer id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public void createNotificationForSpecificUser(Account sender, Account receiver, 
                                                   String title, String content, 
                                                   boolean isPriority) {
        try{
        // Tạo notification
        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(Notification.NotificationTarget.User); // hoặc null nếu muốn
        notification.setStatus("PUBLISHED");
        notification.set_priority(isPriority);
        notification.setCreatedDate(new Date());
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Tạo NotificationReceiver cho User cụ thể
        NotificationReceiver notificationReceiver = new NotificationReceiver();
        notificationReceiver.setReceiver(receiver);
        notificationReceiver.setRead(false);
        notificationReceiver.setNotification(savedNotification);
        notificationReceiverRepository.save(notificationReceiver);

        System.out.println("-------------- thông tin người nhận: " + receiver.getUsername());

        // Gửi real-time notification qua WebSocket
        messagingTemplate.convertAndSendToUser(
            String.valueOf(receiver.getUsername()),
            "/queue/notifications",
            savedNotification.getTitle() + ": " + savedNotification.getContent()
        );
        
        System.out.println("-------------- Đã gửi thông báo tới User: " + receiver.getUsername());
        } catch (Exception e) {
            System.err.println("123456- Lỗi khi gửi thông báo: " + e.getMessage());
        }
    }
    
}
