package com.example.auth_service.service;

import com.example.auth_service.config.AppProperties;
import com.example.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Async
    public void sendVerificationEmail(User user, String token) {
        try {
            String verificationUrl = appProperties.getFrontendUrl() + "/verify-email?token=" + token;

            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("expiryHours", 24);

            String htmlContent = templateEngine.process("email-verification", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Verify your email address - TaskFlow",
                    htmlContent
            );

            log.info("Verification email sent to: {}", maskEmail(user.getEmail()));
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", maskEmail(user.getEmail()), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(User user, String token) {
        try {
            String resetUrl = appProperties.getFrontendUrl() + "/reset-password?token=" + token;

            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("expiryMinutes", 60);

            String htmlContent = templateEngine.process("password-reset", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Reset your password - TaskFlow",
                    htmlContent
            );

            log.info("Password reset email sent to: {}", maskEmail(user.getEmail()));
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", maskEmail(user.getEmail()), e);
        }
    }

    @Async
    public void sendPasswordResetConfirmationEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("loginUrl", appProperties.getFrontendUrl() + "/login");

            String htmlContent = templateEngine.process("password-reset-confirmation", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Your password has been reset - TaskFlow",
                    htmlContent
            );

            log.info("Password reset confirmation email sent to: {}", maskEmail(user.getEmail()));
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", maskEmail(user.getEmail()), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send email to: {}", maskEmail(to), e);
            throw e;
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
