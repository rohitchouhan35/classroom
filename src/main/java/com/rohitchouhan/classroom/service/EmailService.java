package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.model.User;

import org.thymeleaf.context.Context;
import java.util.List;

public interface EmailService {

    public void sendBulkEmails(List<String> emailAddresses, String subject, String body);
    public void sendEmail(String emailAddress, String subject, String body);
    public void sendEmailWithHtmlTemplate(String emailAddress, String subject, String templateName, Context context);

}
