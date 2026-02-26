package com.checkpoint.api.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.checkpoint.api.services.EmailService;

/**
 * Implementation of {@link EmailService} using {@link JavaMailSender}.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@checkpoint.com");
            message.setTo(to);
            message.setSubject("Reset Your Password - Checkpoint");
            message.setText("Hello,\n\n"
                    + "You have requested to reset your password. Please click the link below to set a new password:\n\n"
                    + resetLink + "\n\n"
                    + "This link will expire in 15 minutes.\n\n"
                    + "If you did not request this, please ignore this email.\n\n"
                    + "Best regards,\n"
                    + "The Checkpoint Team");

            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (MailException ex) {
            log.error("Failed to send password reset email to {}", to, ex);
            // We could rethrow or just log. For password reset MVP, logging error is often preferred so we don't break the flow.
        }
    }
}
