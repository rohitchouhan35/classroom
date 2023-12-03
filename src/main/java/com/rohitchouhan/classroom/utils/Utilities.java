package com.rohitchouhan.classroom.utils;

import com.rohitchouhan.classroom.model.Assignment;
import com.rohitchouhan.classroom.model.User;
import com.rohitchouhan.classroom.repository.AssignmentRepository;
import com.rohitchouhan.classroom.service.AssignmentServiceImpl;
import com.rohitchouhan.classroom.service.EmailService;
import com.rohitchouhan.classroom.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class Utilities {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ASSIGNMENT_KEY = "assignmentsCache";
    private static final String STUDENT_EMAIL_LIST_KEY = "studentEmailListKey";

    @Value("${app.cache.expiration-minutes}")
    private int cacheExpirationMinutes;

    public List<String> getAllStudentEmails() {
        List<String> studentEmails = null;

        try {
            studentEmails = (List<String>) redisTemplate.opsForValue().get(STUDENT_EMAIL_LIST_KEY);

            if (studentEmails != null) {
                logger.info("Fetched student emails from cache.");
                return studentEmails;
            }

            logger.info("Cache miss, fetching student emails from the database...");

            List<User> students = userService.getAllStudents();

            if (students != null && students.size() > 0) {
                studentEmails = new ArrayList<>();

                for (User user : students) {
                    String studentEmail = user.getEmail();
                    if (studentEmail != null) {
                        studentEmails.add(studentEmail);
                    }
                }

                if (studentEmails != null && !studentEmails.isEmpty()) {
                    redisTemplate.opsForValue().set(STUDENT_EMAIL_LIST_KEY, studentEmails);
                    redisTemplate.expire(STUDENT_EMAIL_LIST_KEY, cacheExpirationMinutes, TimeUnit.MINUTES);
                }
            } else {
                logger.info("No students found in the database.");
            }
            return studentEmails;
        } catch (Exception e) {
            logger.error("Error while fetching student emails: " + e.getMessage(), e);
            throw new RuntimeException("Error while fetching student emails", e);
        }
    }

    public String createAssignmentEmailBody(Assignment assignment) {
        String formattedBody = "Assignment: " + assignment.getAssignmentName() + "\n\n";
        formattedBody += "---------------------------------------\n";
        formattedBody += "Description:\n" + assignment.getDescription() + "\n\n";
        formattedBody += "---------------------------------------\n";
        formattedBody += "Issued Time: " + assignment.getIssuedTime() + "\n";
        formattedBody += "Due Date: " + assignment.getDueDate();

        return formattedBody;
    }

}
