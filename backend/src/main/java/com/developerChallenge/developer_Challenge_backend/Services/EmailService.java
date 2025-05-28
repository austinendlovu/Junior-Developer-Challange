package com.developerChallenge.developer_Challenge_backend.Services;

import com.developerChallenge.developer_Challenge_backend.Models.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

   
    public void sendLessonReminderEmail(String to, String subject, Map<String, Object> templateModel) {
        Context context = new Context();
        context.setVariables(templateModel);

        String htmlContent = templateEngine.process("lesson-reminder", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Use your verified Gmail address here as from
            helper.setFrom(new InternetAddress("mukomiaustine8@gmail.com", "Timeback Scheduler"));

            mailSender.send(message);

            logger.info("Email sent successfully to {} with subject '{}'", to, subject);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

   
    public void sendLessonReminderEmail(Lesson lesson, String subject) {
        if (lesson.getTeacher() == null || lesson.getTeacher().getEmail() == null) {
            throw new IllegalArgumentException("Lesson must have a teacher with an email");
        }

        Map<String, Object> templateModel = Map.of(
                "teacherName", lesson.getTeacher().getUsername(),
                "subject", lesson.getSubject(),
                "description", lesson.getDescription(),
                "date", lesson.getDate().toString(),
                "startTime", lesson.getStartTime().toString(),
                "endTime", lesson.getEndTime().toString(),
                "classroom", lesson.getClassroom()
        );

        sendLessonReminderEmail(lesson.getTeacher().getEmail(), subject, templateModel);
    }
}
