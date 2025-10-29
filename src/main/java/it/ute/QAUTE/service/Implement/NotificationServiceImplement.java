package it.ute.QAUTE.service.Implement;

import com.github.benmanes.caffeine.cache.Cache;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Event;
import it.ute.QAUTE.entity.EventRegistration;
import it.ute.QAUTE.entity.Notification;
import it.ute.QAUTE.entity.NotificationReceiver;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.repository.EventRegistrationRepository;
import it.ute.QAUTE.repository.NotificationReceiverRepository;
import it.ute.QAUTE.repository.NotificationRepository;
import it.ute.QAUTE.service.EmailService;
import it.ute.QAUTE.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class NotificationServiceImplement implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private NotificationReceiverRepository notificationReceiverRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private EmailService emailService;
    @Autowired
    private Cache<Integer, Boolean> onlineCache;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Override
    public Page<Notification> findAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    @Override
    public Notification findNotificationByNotificationReceiverId(Long id) {
        NotificationReceiver notificationReceiver = notificationReceiverRepository.findById(id);
        if (!notificationReceiver.isRead()) {
            notificationReceiver.setRead(true);
            notificationReceiverRepository.save(notificationReceiver);
        }
        return notificationReceiver.getNotification();
    }

    @Override
    public List<NotificationReceiver> findNotificationByAccountId(long receiverId) {
        return notificationReceiverRepository.findByAccountId(receiverId);
    }

    @Override
    public Page<Notification> findNotificationsBySenderId(String q, String status, long senderId, Pageable pageable) {
        return notificationRepository.searchNotificationsBySenderId(q, status, senderId, pageable);
    }

    @Override
    public Page<Notification> findNotifications(String q, String status, Pageable pageable) {
        return notificationRepository.searchNotifications(q, status, pageable);
    }

    @Override
    public boolean deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(Math.toIntExact(id)).orElse(null);
        if (notification == null || notification.getStatus().equals("PUBLISHED")) {
            return false;
        }
        notificationRepository.delete(notification);
        return true;
    }

    @Override
    public void updateNotification(Long id, String title, String content, String targetType, String status,
                                   boolean is_priority) {
        Notification notification = notificationRepository.findByNotificationID(id);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(Notification.NotificationTarget.valueOf(targetType));
        notification.setStatus(status);
        notification.set_priority(is_priority);
        Notification savedNotification = notificationRepository.save(notification);
        Account account = accountRepository.findByAccountID(notification.getSender().getAccountID());
        sendByRole(savedNotification, targetType, status, account.getRole());
        if (status.equals("DRAFT"))
            deleteNotificationReceiverByNotificationId(id);
    }

    @Override
    public void deleteNotificationReceiverByNotificationId(Long notificationId) {
        notificationReceiverRepository.deleteAllByNotificationId(notificationId);
    }

    @Override
    public void createNotification(Account sender, String title, String content, String targetType, String status,
                                   boolean is_priority) {
        Notification notification = new Notification();
        notification.setContent(content);
        notification.setSender(sender);
        notification.set_priority(is_priority);
        notification.setTitle(title);
        notification.setTargetType(Notification.NotificationTarget.valueOf(targetType));
        notification.setStatus(status);
        notification.setCreatedDate(new Date());
        Notification savedNotification = notificationRepository.save(notification);
        sendByRole(savedNotification, targetType, status, sender.getRole());
    }

    @Override
    public void sendByRole(Notification savedNotification, String targetType, String status, Account.Role roleSender) {
        if ("PUBLISHED".equalsIgnoreCase(status)) {
            List<Account> receivers;
            if ("ALL".equalsIgnoreCase(targetType)) {
                if (roleSender.equals(Account.Role.Admin))
                    receivers = accountRepository.findAllExcludeAdmin();
                else
                    receivers = accountRepository.findUserAndConsultant();
            } else {
                receivers = accountRepository.findByRoleExcludeAdmin(Account.Role.valueOf(targetType));
            }
            for (Account receiver : receivers) {
                NotificationReceiver notificationReceiver = new NotificationReceiver();
                notificationReceiver.setReceiver(receiver);
                notificationReceiver.setRead(false);
                notificationReceiver.setNotification(savedNotification);
                notificationReceiverRepository.save(notificationReceiver);
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(receiver.getUsername()),
                        "/queue/notifications",
                        savedNotification.getTitle() + ": " + savedNotification.getContent());
                Boolean isOnline = onlineCache.getIfPresent(receiver.getAccountID());
                if(!savedNotification.getSender().getRole().equals(Account.Role.System) && savedNotification.is_priority() && isOnline==null) emailService.sendNotification(receiver.getEmail(), savedNotification.getTitle(), savedNotification.getContent());
            }
            System.out.println("✅ Gửi thông báo tới " + receivers.size() + " người dùng.");
        } else {
            System.out.println("lưu nháp");
        }
    }
    @Override
    public Notification findNotificationById(Integer id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public Account getSystemSender() {
        return accountRepository.findFirstSystem();
    }
    public void notifyManagerViolation(String titleQuestion, String question, LocalDateTime senderDate) {
        String title = "⚠️ Cảnh báo: Phát hiện nội dung vi phạm tiêu chuẩn cộng đồng";
        String content = String.format(
                """
                Hệ thống QAUTE đã phát hiện câu hỏi có dấu hiệu vi phạm tiêu chuẩn cộng đồng.
                
                📌 **Thông tin chi tiết:**
                • Tiêu đề câu hỏi: %s
                • Nội dung: "%s"
                • Thời gian đăng: %s
                
                🔍 **Hành động cần thực hiện:**
                - Xem xét nội dung câu hỏi
                - Quyết định xóa hoặc giữ lại câu hỏi
                - Cân nhắc cảnh báo hoặc xử phạt người dùng nếu cần
                
                ⚠️ Vui lòng kiểm tra và xử lý trong thời gian sớm nhất để đảm bảo môi trường cộng đồng lành mạnh.
                
                — QAUTE Moderation System
                """,
                titleQuestion,
                question,
                senderDate.format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"))
        );
        createNotification(getSystemSender(), title, content, "Manager", "PUBLISHED", false);
    }
    @Override
    public void notifyAdminSuspiciousActivityAlert(String ipAddress, String deviceName, String activity, String reason) {
        String title = "🚨 Cảnh báo bảo mật: Phát hiện hoạt động đáng ngờ";

        String content = String.format(
                """
                Hệ thống QAUTE đã phát hiện hoạt động đáng ngờ có thể là tấn công bảo mật!
                
                📌 **Thông tin chi tiết:**
                • Địa chỉ IP: %s
                • Thiết bị: %s
                • Hoạt động: %s
                • Lý do cảnh báo: %s
                • Thời gian phát hiện: %s
                
                ⚠️ **Hành động khuyến nghị:**
                - Kiểm tra logs hệ thống ngay lập tức
                - Xem xét chặn IP này nếu cần thiết
                - Giám sát các hoạt động tiếp theo từ thiết bị này
                
                🔒 Hệ thống đã tự động áp dụng các biện pháp bảo vệ tạm thời.
                
                — QAUTE Security System
                """,
                ipAddress,
                deviceName,
                activity,
                reason,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"))
        );
        createNotification(getSystemSender(), title, content, "Admin", "PUBLISHED", true);
    }
    @Override
    public void createNotificationForSpecificUser(Account sender, Account receiver,
                                                  String title, String content,
                                                  boolean isPriority) {

        // Thêm kiểm tra null để đảm bảo an toàn
        if (sender == null || receiver == null) {
            System.err.println("123456- Lỗi khi gửi thông báo: Người gửi hoặc Người nhận là null.");
            return;
        }

        try {
            // Tạo notification
            Notification notification = new Notification();
            notification.setSender(sender);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setTargetType(Notification.NotificationTarget.User); // Gửi 1-1
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
                    savedNotification.getTitle() + ": " + savedNotification.getContent());

            System.out.println("-------------- Đã gửi thông báo từ " + sender.getUsername() + " tới User: "
                    + receiver.getUsername());
        } catch (Exception e) {
            System.err.println("123456- Lỗi khi gửi thông báo: " + e.getMessage());
        }
    }
    public void notifyUserViolation(Account receiver, String titleQuestion, String question, LocalDateTime senderDate) {
        Account accountSystem = accountRepository.findFirstSystem();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

        String title = "⚠️ Cảnh báo: Phát hiện nội dung vi phạm chính sách";

        String body = """
                Hệ thống QAUTE vừa phát hiện nội dung bạn đăng có dấu hiệu vi phạm nguyên tắc cộng đồng.
                
                📌 **Thông tin chi tiết:**
                • Tiêu đề câu hỏi: %s
                • Nội dung: "%s"
                • Thời gian gửi: %s
                
                🚫 Vui lòng xem xét và chỉnh sửa lại câu hỏi của bạn để đảm bảo phù hợp với quy tắc ứng xử trong hệ thống.
                Nếu bạn cho rằng đây là nhầm lẫn, hãy liên hệ với ban quản trị để được hỗ trợ.
                
                — QAUTE System
                """.formatted(titleQuestion, question, senderDate.format(formatter));

        createNotificationForSpecificUser(accountSystem, receiver, title, body, false);
    }
    @Override
    public void notifyManagersNewEvent(Event event) {
        List<Account> managers = accountRepository.findAll().stream()
                .filter(acc -> acc.getRole() == Account.Role.Manager)
                .toList();

        String title = "Sự kiện mới cần duyệt";
        String content = String.format(
                "Tư vấn viên %s đã tạo sự kiện mới: %s. Vui lòng xem xét và phê duyệt.",
                event.getConsultant().getProfile().getFullName(),
                event.getTitle());

        // Người gửi là Tư vấn viên (người tạo sự kiện)
        Account sender = event.getConsultant().getProfile().getAccount();

        for (Account managerReceiver : managers) {
            createNotificationForSpecificUser(
                    sender, // Người gửi là Consultant
                    managerReceiver, // Người nhận là Manager
                    title,
                    content,
                    false);
        }
    }

    @Override
    public void notifyManagersEventUpdated(Event event) {
        List<Account> managers = accountRepository.findAll().stream()
                .filter(acc -> acc.getRole() == Account.Role.Manager)
                .toList();

        String title = "Sự kiện đã được cập nhật";
        String content = String.format(
                "Tư vấn viên %s đã cập nhật sự kiện: %s. Vui lòng duyệt lại.",
                event.getConsultant().getProfile().getFullName(),
                event.getTitle());

        // Người gửi là Tư vấn viên (người cập nhật)
        Account sender = event.getConsultant().getProfile().getAccount();

        for (Account managerReceiver : managers) {
            createNotificationForSpecificUser(
                    sender, // Người gửi là Consultant
                    managerReceiver, // Người nhận là Manager
                    title,
                    content,
                    false);
        }
    }

    @Override
    public void notifyConsultantEventApproved(Event event) {
        // Người nhận là Tư vấn viên
        Account consultantReceiver = event.getConsultant().getProfile().getAccount();
        // Người gửi là Hệ thống (Admin/Manager)
        Account systemSender = getSystemSender();

        String title = "Sự kiện đã được phê duyệt";
        String content = String.format(
                "Chúc mừng! Sự kiện '%s' của bạn đã được phê duyệt và công khai. " +
                        "Người dùng giờ đây có thể đăng ký tham gia.",
                event.getTitle());

        createNotificationForSpecificUser(
                systemSender,
                consultantReceiver,
                title,
                content,
                false);
    }

    @Override
    public void notifyConsultantEventRejected(Event event, String reason) {
        // Người nhận là Tư vấn viên
        Account consultantReceiver = event.getConsultant().getProfile().getAccount();
        // Người gửi là Hệ thống (Admin/Manager)
        Account systemSender = getSystemSender();

        String title = "Sự kiện bị từ chối";
        String content = String.format(
                "Rất tiếc, sự kiện '%s' của bạn đã bị từ chối. Lý do: %s. " +
                        "Bạn có thể chỉnh sửa và gửi lại.",
                event.getTitle(),
                reason);

        createNotificationForSpecificUser(
                systemSender,
                consultantReceiver,
                title,
                content,
                false);
    }

    @Override
    public void notifyConsultantNewRegistration(Event event, User user) {
        // Người nhận là Tư vấn viên
        Account consultantReceiver = event.getConsultant().getProfile().getAccount();
        // Người gửi là User (người vừa đăng ký)
        Account userSender = user.getProfile().getAccount();

        String title = "Có người đăng ký sự kiện";
        String content = String.format(
                "%s vừa đăng ký tham gia sự kiện '%s' của bạn. " +
                        "Hiện có %d/%s người đăng ký.",
                user.getProfile().getFullName(),
                event.getTitle(),
                event.getCurrentParticipants(),
                event.getMaxParticipants() != null ? event.getMaxParticipants().toString() : "không giới hạn");

        createNotificationForSpecificUser(
                userSender,
                consultantReceiver,
                title,
                content,
                false);
    }

    @Override
    public void notifyUserRegistrationSuccess(User user, Event event) {
        // Người nhận là User
        Account userReceiver = user.getProfile().getAccount();
        // Người gửi là Hệ thống (Admin/Manager)
        Account systemSender = getSystemSender();

        String title = "Đăng ký sự kiện thành công";
        String content = String.format(
                "Bạn đã đăng ký thành công sự kiện '%s'. " +
                        "Thời gian: %s. Hãy đánh dấu lịch của bạn!",
                event.getTitle(),
                event.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        createNotificationForSpecificUser(
                systemSender,
                userReceiver,
                title,
                content,
                false);
    }

    @Override
    public void notifyUserEventCancelled(User user, Event event, String reason) {
        // Người nhận là User
        Account userReceiver = user.getProfile().getAccount();
        // Người gửi là Hệ thống (Admin/Manager)
        Account systemSender = getSystemSender();

        String title = "Sự kiện đã bị hủy";
        String content = String.format(
                "Rất tiếc, sự kiện '%s' mà bạn đã đăng ký đã bị hủy. Lý do: %s. " +
                        "Chúng tôi xin lỗi vì sự bất tiện này.",
                event.getTitle(),
                reason);

        createNotificationForSpecificUser(
                systemSender,
                userReceiver,
                title,
                content,
                false);
    }

    @Override
    public void notifyUserEventReminder(User user, Event event) {
        // Người nhận là User
        Account userReceiver = user.getProfile().getAccount();
        // Người gửi là Hệ thống (Admin/Manager)
        Account systemSender = getSystemSender();

        String title = "Nhắc nhở sự kiện";
        String content = String.format(
                "Sự kiện '%s' sẽ bắt đầu vào %s (còn 24 giờ nữa). " +
                        "Hãy chuẩn bị sẵn sàng!",
                event.getTitle(),
                event.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        createNotificationForSpecificUser(
                systemSender,
                userReceiver,
                title,
                content,
                false);
    }

    @Override
    public void notifyUsersEventStartingSoon(Event event) {
        List<EventRegistration> registrations = eventRegistrationRepository.findActiveRegistrations(event);
        // Người gửi là Hệ thống (Admin/Manager)
        Account systemSender = getSystemSender();

        String title = "Sự kiện sắp bắt đầu";
        String content = String.format(
                "Sự kiện '%s' sẽ bắt đầu sau 15 phút. %s",
                event.getTitle(),
                event.getMeetingLink() != null ? "Link tham gia: " + event.getMeetingLink()
                        : event.getLocation() != null ? "Địa điểm: " + event.getLocation() : "");

        for (EventRegistration registration : registrations) {
            // Người nhận là User
            Account userReceiver = registration.getUser().getProfile().getAccount();
            createNotificationForSpecificUser(
                    systemSender,
                    userReceiver,
                    title,
                    content,
                    false);
        }
    }

    @Override
    public void notifyUserRegistrationConfirmed(User user, Event event) {
        // Người nhận là User
        Account userReceiver = user.getProfile().getAccount();
        // Người gửi là Hệ thống (Admin/Manager)
        Account systemSender = getSystemSender();

        String title = "Đăng ký sự kiện đã được xác nhận";
        String content = String.format(
                "Chúc mừng! Đăng ký của bạn cho sự kiện '%s' đã được xác nhận. " +
                        "Thời gian: %s.",
                event.getTitle(),
                event.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        createNotificationForSpecificUser(
                systemSender,
                userReceiver,
                title,
                content,
                false);
    }

    @Override
    public long countAll() {
        return notificationRepository.count();
    }
}
