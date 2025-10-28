package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailServiceImplement implements EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private ApplicationContext context;
    @Autowired @org.springframework.context.annotation.Lazy
    private EmailService self;
    @Override
    public String createOTP(){
        SecureRandom random = new SecureRandom();
        String DIGITS = "0123456789";
        int OTP_LENGTH = 6;
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }
    @org.springframework.scheduling.annotation.Async("mailExecutor")
    @Override
    public void sendEmail(String toEmail, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
    @Override
    public String sendForgetPasswordEmail(String toEmail) {
        String otp = createOTP();
        try {
            String htmlContent = getEmailTemplate("Lấy lại mật khẩu", getForgetPasswordOTPContent(otp));
            self.sendEmailHtml(toEmail, "Lấy lại mật khẩu QAUTE", htmlContent);
        } catch (MessagingException e) {
            // Fallback to plain text email if HTML fails
            self.sendEmail(toEmail, "Lấy lại mật khẩu", "Xin chào,\n\nMã OTP của bạn là: " + otp + "\n\nMã có hiệu lực trong 3 phút.");
        }
        return otp;
    }
    @Override
    public String sendRegisterEmail(String toEmail) {
        String otp = createOTP();
        try {
            String htmlContent = getEmailTemplate("Đăng ký tài khoản", getRegisterOTPContent(otp));
            self.sendEmailHtml(toEmail, "Xác nhận đăng ký tài khoản QAUTE", htmlContent);
        } catch (MessagingException e) {
            self.sendEmail(toEmail, "Đăng ký tài khoản", "Xin chào,\n\nMã OTP của bạn là: " + otp + "\n\nMã có hiệu lực trong 3 phút.");
        }
        return otp;
    }
    @Override
    public String sendChangePassword(String toEmail){
        String otp = createOTP();
        try {
            String htmlContent = getEmailTemplate("Đổi mật khẩu", getChangePasswordOTPContent(otp));
            self.sendEmailHtml(toEmail, "Lấy lại mật khẩu QAUTE", htmlContent);
            return otp;
        } catch (MessagingException e) {
            // Fallback to plain text email if HTML fails
            self.sendEmail(toEmail, "Đổi mật khẩu", "Xin chào,\n\nMã OTP của bạn là: " + otp + "\n\nMã có hiệu lực trong 3 phút.");
        }
        return otp;
    }
    @Override
    public String sendMFAOTP(String toEmail){
        String otp = createOTP();
        try {
            String htmlContent = getEmailTemplate("Xác thực đăng nhập", getMFAOTPContent(otp));
            self.sendEmailHtml(toEmail, "Xác thực đăng nhập QAUTE", htmlContent);
            return otp;
        } catch (MessagingException e) {
            // Fallback to plain text email if HTML fails
            self.sendEmail(toEmail, "Xác thực đăng nhập", "Xin chào,\n\nMã OTP xác thực đăng nhập của bạn là: " + otp + "\n\nMã có hiệu lực trong 3 phút.");
        }
        return otp;
    }
    @Override
    public void sendNotification(String toEmail, String header, String body) {
        try {
            String htmlContent = getEmailTemplate(header, getSystemNotificationContent(header, body));
            self.sendEmailHtml(toEmail, header + " - QAUTE", htmlContent);
        } catch (MessagingException e) {
            self.sendEmail(toEmail, header, "Xin chào,\n\n" + body + "\n\nTrân trọng,\nQAUTE");
        }
    }
    @Override
    public void sendSuspiciousActivityAlert(String toEmail, String ipAddress, String deviceName, String activity, String reason) {
        try {
            String htmlContent = getEmailTemplate("🔍 HOẠT ĐỘNG BẤT THƯỜNG",
                    getSuspiciousActivityContent(ipAddress, deviceName, activity, reason));
            self.sendEmailHtml(toEmail, "🔍 PHÁT HIỆN HOẠT ĐỘNG BẤT THƯỜNG", htmlContent);
        } catch (MessagingException e) {
            String plainText = String.format(
                    "HOẠT ĐỘNG BẤT THƯỜNG\n\n" +
                            "Tên thiết bị: %s\n" +
                            "Hoạt động: %s\n" +
                            "Lý do cảnh báo: %s\n" +
                            "Địa chỉ IP: %s\n\n" +
                            "Vui lòng xem xét!",
                    deviceName, activity, reason, ipAddress
            );
            self.sendEmail(toEmail, "🔍 HOẠT ĐỘNG BẤT THƯỜNG", plainText);
        }
    }
    @org.springframework.scheduling.annotation.Async("mailExecutor")
    @Override
    public void sendEmailHtml(String toEmail, String subject, String htmlBody)
            throws MessagingException {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(mime);
    }
    @Override
    public String getSuspiciousActivityContent(String ipAddress, String deviceName, String activity, String reason) {
        return "<h2 style='color: #ffc107;'>🔍 Hoạt Động Bất Thường</h2>" +
                "<p>Hệ thống phát hiện hoạt động khả nghi cần xem xét!</p>" +
                "<div class='otp-box' style='background: linear-gradient(135deg, #fffbea 0%, #fffef0 100%); border: 3px solid #ffc107;'>" +
                "    <div class='otp-icon'>👤</div>" +
                "    <div class='otp-label' style='color: #ffc107;'>THÔNG TIN NGƯỜI DÙNG</div>" +
                "    <div style='text-align: left; padding: 20px; color: #333;'>" +
                "        <p><strong>👤 Tên thiết bị:</strong> " + deviceName + "</p>" +
                "        <p><strong>🎬 Hoạt động:</strong> " + activity + "</p>" +
                "        <p><strong>❓ Lý do cảnh báo:</strong> " + reason + "</p>" +
                "        <p><strong>🌐 Địa chỉ IP:</strong> <code style='background: #f8f9fa; padding: 5px 10px; border-radius: 5px;'>" + ipAddress + "</code></p>" +
                "    </div>" +
                "</div>" +
                "<div class='info-box'>" +
                "    <p>💡 Xem xét khóa tài khoản tạm thời nếu phát hiện vi phạm nghiêm trọng.</p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p style='color: #999999; font-size: 14px; text-align: center;'>" +
                "    <strong>QAUTE Activity Monitor</strong>" +
                "</p>";
    }
    @Override
    public String getMFAOTPContent(String otp) {
        return "<h2>Xác thực đăng nhập</h2>" +
                "<p>Xin chào! 👋</p>" +
                "<p>Chúng tôi phát hiện một yêu cầu đăng nhập vào tài khoản của bạn tại <strong>QAUTE</strong>. " +
                "Để bảo mật tài khoản, vui lòng sử dụng mã OTP bên dưới để hoàn tất đăng nhập:</p>" +
                "<div class='otp-box'>" +
                "    <div class='otp-icon'>🔒</div>" +
                "    <div class='otp-label'>Mã Xác Thực OTP</div>" +
                "    <div class='otp-code'>" + otp + "</div>" +
                "</div>" +
                "<div class='warning'>" +
                "    <p><strong>⚠️ Lưu ý quan trọng:</strong> Mã OTP này có hiệu lực trong <strong>3 phút</strong>. " +
                "    Vui lòng không chia sẻ mã này với bất kỳ ai để bảo vệ tài khoản của bạn.</p>" +
                "</div>" +
                "<div class='info-box'>" +
                "    <p>💡 Bạn không thực hiện đăng nhập này? Tài khoản của bạn có thể bị xâm nhập. " +
                "    Hãy đổi mật khẩu ngay và liên hệ với chúng tôi để được hỗ trợ!</p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p style='color: #999999; font-size: 14px; text-align: center;'>" +
                "    <strong>Bảo mật tài khoản</strong><br>" +
                "    Luôn bật xác thực 2 yếu tố và không chia sẻ mã OTP với bất kỳ ai" +
                "</p>";
    }
    @Override
    public String getEmailTemplate(String title, String content) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>" + title + "</title>" +
                "    <style>" +
                "        body { margin: 0; padding: 20px; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7fa; }" +
                "        .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; box-shadow: 0 10px 40px rgba(13, 110, 253, 0.3); border: 2px solid #0d6efd; border-radius: 20px; overflow: hidden; }" +
                "        .header { background: linear-gradient(135deg, #0d6efd 0%, #0dcaf0 100%); padding: 50px 40px; text-align: center; position: relative; overflow: hidden; }" +
                "        .header::before { content: ''; position: absolute; top: -50%; right: -50%; width: 200%; height: 200%; background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%); }" +
                "        .header h1 { color: #ffffff; margin: 0; font-size: 42px; font-weight: 800; text-shadow: 0 4px 10px rgba(0,0,0,0.3); letter-spacing: 2px; position: relative; z-index: 1; }" +
                "        .header .icon { font-size: 64px; margin-bottom: 15px; filter: drop-shadow(0 5px 10px rgba(0,0,0,0.3)); position: relative; z-index: 1; display: inline-block; }" +
                "        .header .subtitle { color: rgba(255,255,255,0.9); font-size: 14px; margin-top: 10px; letter-spacing: 3px; text-transform: uppercase; position: relative; z-index: 1; }" +
                "        .content { padding: 50px 40px; background: linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%); }" +
                "        .content h2 { color: #0d6efd; margin-top: 0; font-size: 28px; font-weight: 700; margin-bottom: 20px; }" +
                "        .content p { color: #666666; line-height: 1.8; font-size: 16px; margin-bottom: 15px; }" +
                "        .otp-box { background: linear-gradient(135deg, #e3f2fd 0%, #f0f8ff 100%); border: 3px solid #0d6efd; border-radius: 20px; padding: 40px 30px; text-align: center; margin: 40px 0; box-shadow: 0 8px 30px rgba(13, 110, 253, 0.25); position: relative; overflow: hidden; }" +
                "        .otp-code { font-size: 48px; font-weight: 900; background: linear-gradient(135deg, #0d6efd 0%, #0dcaf0 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; letter-spacing: 15px; margin: 20px 0; position: relative; z-index: 1; font-family: 'Courier New', monospace; }" +
                "        .otp-label { color: #0d6efd; font-size: 13px; font-weight: 700; margin-bottom: 15px; text-transform: uppercase; letter-spacing: 3px; position: relative; z-index: 1; }" +
                "        .otp-icon { font-size: 32px; margin-bottom: 10px; position: relative; z-index: 1; }" +
                "        .warning { background: linear-gradient(135deg, #fff9e6 0%, #fffbea 100%); border-left: 5px solid #ffc107; border-radius: 12px; padding: 25px; margin: 30px 0; box-shadow: 0 4px 15px rgba(255, 193, 7, 0.2); position: relative; }" +
                "        .warning::before { content: '⚠️'; position: absolute; right: 20px; top: 20px; font-size: 32px; opacity: 0.3; }" +
                "        .warning p { color: #856404; margin: 0; font-size: 15px; line-height: 1.6; }" +
                "        .info-box { background: linear-gradient(135deg, #e7f3ff 0%, #f0f8ff 100%); border-radius: 12px; padding: 20px; margin: 25px 0; border: 2px dashed #0d6efd; text-align: center; }" +
                "        .info-box p { color: #0d6efd; margin: 0; font-size: 14px; font-weight: 600; }" +
                "        .footer { background: linear-gradient(180deg, #1e3a8a 0%, #1e40af 100%); padding: 40px 30px; text-align: center; position: relative; }" +
                "        .footer::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, #0d6efd, #0dcaf0, #0d6efd); }" +
                "        .footer p { color: rgba(255,255,255,0.8); font-size: 14px; margin: 8px 0; }" +
                "        .footer p strong { color: #ffffff; font-size: 16px; }" +
                "        .social-links { margin: 20px 0; display: flex; justify-content: center; gap: 15px; }" +
                "        .social-links a { display: inline-block; width: 40px; height: 40px; background: rgba(255,255,255,0.1); border-radius: 50%; line-height: 40px; color: white; text-decoration: none; }" +
                "        .divider { height: 2px; background: linear-gradient(90deg, transparent 0%, #0d6efd 50%, transparent 100%); margin: 35px 0; position: relative; }" +
                "        .divider::after { content: '◆'; position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); background: white; color: #0d6efd; padding: 0 10px; font-size: 10px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='email-container'>" +
                "        <div class='header'>" +
                "            <div class='icon'>🎓</div>" +
                "            <h1>QAUTE</h1>" +
                "            <div class='subtitle'>Hệ thống tư vấn trực tuyến</div>" +
                "        </div>" +
                "        <div class='content'>" +
                content +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p><strong>QAUTE - Hệ Thống Tư Vấn Trực Tuyến</strong></p>" +
                "            <div class='social-links'>" +
                "                <a href='#'>📘</a>" +
                "                <a href='#'>🐦</a>" +
                "                <a href='#'>📷</a>" +
                "                <a href='#'>💼</a>" +
                "            </div>" +
                "            <p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "            <p style='color: rgba(255,255,255,0.5); margin-top: 15px; font-size: 12px;'>© 2025 QAUTE. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    // Template cho email OTP đăng ký
    @Override
    public String getRegisterOTPContent(String otp) {
        return "<h2>Xác nhận đăng ký tài khoản</h2>" +
                "<p>Xin chào! 👋</p>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>QAUTE</strong>. Để hoàn tất quá trình đăng ký, vui lòng sử dụng mã OTP bên dưới:</p>" +
                "<div class='otp-box'>" +
                "    <div class='otp-icon'>🔐</div>" +
                "    <div class='otp-label'>Mã Xác Thực OTP</div>" +
                "    <div class='otp-code'>" + otp + "</div>" +
                "</div>" +
                "<div class='warning'>" +
                "    <p><strong>⚠️ Lưu ý quan trọng:</strong> Mã OTP này có hiệu lực trong <strong>3 phút</strong>. Vui lòng không chia sẻ mã này với bất kỳ ai để bảo vệ tài khoản của bạn.</p>" +
                "</div>" +
                "<div class='info-box'>" +
                "    <p>💡 Bạn chưa yêu cầu đăng ký? Hãy bỏ qua email này.</p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p style='color: #999999; font-size: 14px; text-align: center;'>" +
                "    <strong>Cần hỗ trợ?</strong><br>" +
                "    Liên hệ với chúng tôi qua email support@qaute.edu.vn" +
                "</p>";
    }
    @Override
    public String getForgetPasswordOTPContent(String otp) {
        return "<h2>Lấy lại mật khẩu</h2>" +
                "<p>Xin chào! 👋</p>" +
                "<p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại <strong>QAUTE</strong>. Vui lòng sử dụng mã OTP bên dưới để tiếp tục:</p>" +
                "<div class='otp-box'>" +
                "    <div class='otp-icon'>🔑</div>" +
                "    <div class='otp-label'>Mã Xác Thực OTP</div>" +
                "    <div class='otp-code'>" + otp + "</div>" +
                "</div>" +
                "<div class='warning'>" +
                "    <p><strong>⚠️ Lưu ý quan trọng:</strong> Mã OTP này có hiệu lực trong <strong>3 phút</strong>. Vui lòng không chia sẻ mã này với bất kỳ ai để bảo vệ tài khoản của bạn.</p>" +
                "</div>" +
                "<div class='info-box'>" +
                "    <p>💡 Bạn chưa yêu cầu đặt lại mật khẩu? Hãy liên hệ ngay với chúng tôi!</p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p style='color: #999999; font-size: 14px; text-align: center;'>" +
                "    <strong>Bảo mật tài khoản</strong><br>" +
                "    Đừng chia sẻ mã OTP với bất kỳ ai, kể cả nhân viên QAUTE" +
                "</p>";
    }
    @Override
    public String getChangePasswordOTPContent(String otp) {
        return "<h2>Xác nhận đổi mật khẩu</h2>" +
                "<p>Xin chào! 👋</p>" +
                "<p>Bạn đang thực hiện thao tác <strong>đổi mật khẩu</strong> tại <strong>QAUTE</strong>. " +
                "Để bảo mật tài khoản, vui lòng sử dụng mã OTP bên dưới để xác nhận:</p>" +
                "<div class='otp-box'>" +
                "    <div class='otp-icon'>🔐</div>" +
                "    <div class='otp-label'>Mã Xác Thực OTP</div>" +
                "    <div class='otp-code'>" + otp + "</div>" +
                "</div>" +
                "<div class='warning'>" +
                "    <p><strong>⚠️ Lưu ý quan trọng:</strong> Mã OTP này có hiệu lực trong <strong>3 phút</strong>. " +
                "    Vui lòng không chia sẻ mã này với bất kỳ ai để bảo vệ tài khoản của bạn.</p>" +
                "</div>" +
                "<div class='info-box'>" +
                "    <p>💡 Bạn không yêu cầu đổi mật khẩu? Tài khoản của bạn có thể bị xâm nhập. " +
                "    Hãy liên hệ ngay với chúng tôi để được hỗ trợ!</p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p style='color: #999999; font-size: 14px; text-align: center;'>" +
                "    <strong>Mẹo bảo mật</strong><br>" +
                "    Sử dụng mật khẩu mạnh với ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt" +
                "</p>";
    }
    @Override
    public String getSystemNotificationContent(String title, String messageBody) {
        return "<h2>" + title + "</h2>" +
                "<p>Xin chào bạn 👋</p>" +
                "<p>Đây là thông báo quan trọng từ hệ thống <strong>QAUTE</strong>:</p>" +
                "<div class='otp-box'>" +
                "    <div class='otp-icon'>📢</div>" +
                "    <div class='otp-label'>Nội dung thông báo</div>" +
                "    <div class='otp-code' " +
                "         style='font-size: 20px; letter-spacing: 1px; line-height: 1.8; font-family: Segoe UI, Arial, sans-serif; " +
                "                background: none; color: #0d6efd; text-shadow: none;'>" +
                messageBody +
                "    </div>" +
                "</div>" +
                "<div class='info-box'>" +
                "    <p>🔔 Hãy đảm bảo bạn đã đọc kỹ thông tin và cập nhật kế hoạch của mình nếu cần.</p>" +
                "</div>" +
                "<div class='divider'></div>" +
                "<p style='color: #999999; font-size: 14px; text-align: center;'>" +
                "    <strong>QAUTE - Hệ thống tư vấn & thông báo trực tuyến</strong><br>" +
                "    Mọi thắc mắc xin liên hệ: <a href='mailto:support@qaute.edu.vn'>support@qaute.edu.vn</a>" +
                "</p>";
    }
}
