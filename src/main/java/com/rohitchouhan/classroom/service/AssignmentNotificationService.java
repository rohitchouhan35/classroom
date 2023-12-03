package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.model.Assignment;
import com.rohitchouhan.classroom.repository.AssignmentRepository;
import com.rohitchouhan.classroom.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentNotificationService.class);

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private Utilities utilities;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
    public void notifyStudentsAboutPendingAssignments() {
        LocalDate today = LocalDate.now();
        LocalDate twoDaysLater = today.plusDays(2);

        List<Assignment> assignmentsWithDueDateTodayOrInFuture = assignmentRepository.findAssignmentsWithDueDateTodayOrInFuture();

        List<Assignment> assignmentsWithDueDateInTwoDaysOrLess = assignmentsWithDueDateTodayOrInFuture.stream()
                .filter(assignment -> assignment.getDueDate().toLocalDate().isBefore(twoDaysLater) || assignment.getDueDate().toLocalDate().isEqual(twoDaysLater))
                .collect(Collectors.toList());

        if (assignmentsWithDueDateInTwoDaysOrLess.isEmpty()) {
            logger.info("No urgent assignment is there!");
            return;
        }

        List<String> allStudentEmails = utilities.getAllStudentEmails();

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Gentle Reminder: Last Date of Submission of Assignments\n\n");

        for (Assignment assignment : assignmentsWithDueDateInTwoDaysOrLess) {
            emailContent.append("Assignment Name: ").append(assignment.getAssignmentName()).append("\n");
            emailContent.append("Assignment ID: ").append(assignment.getId()).append("\n");
            emailContent.append("Last Date of Submission: ").append(assignment.getDueDate()).append("\n\n");
        }

        String subject = "Gentle Reminder: Last Date of Submission of Assignments";
        String body = emailContent.toString();

        emailService.sendBulkEmails(allStudentEmails, subject, body);
    }



}
