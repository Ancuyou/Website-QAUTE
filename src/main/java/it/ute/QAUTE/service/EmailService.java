package it.ute.QAUTE.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    String createOTP();

    @org.springframework.scheduling.annotation.Async("mailExecutor")
    void sendEmail(String toEmail, String subject, String body);

    String sendForgetPasswordEmail(String toEmail);

    String sendRegisterEmail(String toEmail);

    String sendChangePassword(String toEmail);

    String sendMFAOTP(String toEmail);

    void sendNotification(String toEmail, String header, String body);

    void sendSuspiciousActivityAlert(String toEmail, String ipAddress, String deviceName, String activity, String reason);

    @org.springframework.scheduling.annotation.Async("mailExecutor")
    void sendEmailHtml(String toEmail, String subject, String htmlBody)
            throws MessagingException;

    String getSuspiciousActivityContent(String ipAddress, String deviceName, String activity, String reason);

    String getMFAOTPContent(String otp);

    String getEmailTemplate(String title, String content);

    // Template cho email OTP đăng ký
    String getRegisterOTPContent(String otp);

    String getForgetPasswordOTPContent(String otp);

    String getChangePasswordOTPContent(String otp);

    String getSystemNotificationContent(String title, String messageBody);
}
