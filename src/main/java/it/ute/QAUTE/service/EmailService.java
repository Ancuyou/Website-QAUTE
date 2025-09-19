package it.ute.QAUTE.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private ApplicationContext context;
    private String createOTP(){
        SecureRandom random = new SecureRandom();
        String DIGITS = "0123456789";
        int OTP_LENGTH = 6;
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }
    @Async
    public void sendEmail(String toEmail, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
    public String sendEmailOTP(String toEmail){
        String otp = createOTP();
        String body="Xin chào,\n\nMã OTP của bạn là: " + otp + "\n\nMã có hiệu lực trong 5 phút.";
        String subject="Lấy lại mật khẩu";
        context.getBean(EmailService.class).sendEmail(toEmail, subject, body);
        return otp;
    }
}
