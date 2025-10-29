package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
    Page<Notification> findAllNotifications(Pageable pageable);

    Notification findNotificationByNotificationReceiverId(Long id);

    List<NotificationReceiver> findNotificationByAccountId(long receiverId);

    Page<Notification> findNotificationsBySenderId(String q, String status, long senderId, Pageable pageable);

    Page<Notification> findNotifications(String q, String status, Pageable pageable);

    boolean deleteNotification(Long id);

    void updateNotification(Long id, String title, String content, String targetType, String status,
                            boolean is_priority);

    void deleteNotificationReceiverByNotificationId(Long notificationId);

    void createNotification(Account sender, String title, String content, String targetType, String status,
                            boolean is_priority);

    void sendByRole(Notification savedNotification, String targetType, String status, Account.Role roleSender);

    Notification findNotificationById(Integer id);

    Account getSystemSender();

    void createNotificationForSpecificUser(Account sender, Account receiver,
                                           String title, String content,
                                           boolean isPriority);

    void notifyManagersNewEvent(Event event);

    void notifyManagersEventUpdated(Event event);

    void notifyConsultantEventApproved(Event event);

    void notifyConsultantEventRejected(Event event, String reason);

    void notifyConsultantNewRegistration(Event event, User user);

    void notifyUserRegistrationSuccess(User user, Event event);

    void notifyUserEventCancelled(User user, Event event, String reason);

    void notifyUserEventReminder(User user, Event event);

    void notifyUsersEventStartingSoon(Event event);

    void notifyUserRegistrationConfirmed(User user, Event event);

    long countAll();
    void notifyAdminSuspiciousActivityAlert(String ipAddress, String deviceName, String activity, String reason);
    void notifyManagerViolation(String titleQuestion, String question, LocalDateTime senderDate);
}
