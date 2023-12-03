package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.model.User;
import com.rohitchouhan.classroom.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import org.thymeleaf.context.Context;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    @Async
    public void sendBulkEmails(List<String> emailAddresses, String subject, String body) {
        logger.info("Bulk email sending started for {} users", emailAddresses.size());

        Context context = new Context();

        for (String emailAddress : emailAddresses) {
            sendEmail(emailAddress, subject, body);
        }

        logger.info("Bulk email sending completed for {} users", emailAddresses.size());
    }

    public void sendBulkEmails() {
        List<User> allStudents = userRepository.findByRole("student");

    }

    @Override
    @Async
    public void sendEmail(String emailAddress, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailAddress);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            logger.info("Email sent to {}", emailAddress);
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", emailAddress, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendEmailWithHtmlTemplate(String emailAddress, String subject, String templateName, Context context) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(emailAddress);
            helper.setSubject(subject);
            String htmlContent = templateEngine.process(templateName, (IContext) context);
            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
            logger.info("Email with HTML template sent to {}", emailAddress);
        } catch (MessagingException e) {
            logger.error("Error sending email with HTML template to {}: {}", emailAddress, e.getMessage());
        }
    }
}
