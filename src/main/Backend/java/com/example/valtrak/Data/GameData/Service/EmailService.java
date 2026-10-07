package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends verification emails. If no mail server is configured (spring.mail.host),
 * the code is printed to the server console instead, so development works
 * without any email setup.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${valtrak.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendVerificationCode(String to, String displayName, String code) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("[DEV MODE] No mail server configured. Verification code for {}: {}", to, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Your Valtrak verification code");
        message.setText("Hi " + displayName + ",\n\n"
                + "Your Valtrak verification code is: " + code + "\n\n"
                + "It expires in 15 minutes. If you didn't create a Valtrak account, you can ignore this email.");
        try {
            sender.send(message);
        } catch (MailException e) {
            log.error("Failed to send verification email to {}", to, e);
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "We couldn't send the verification email. Please try again later.");
        }
    }
}
