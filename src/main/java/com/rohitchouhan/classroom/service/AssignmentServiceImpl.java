package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.model.Assignment;
import com.rohitchouhan.classroom.model.User;
import com.rohitchouhan.classroom.repository.AssignmentRepository;
import com.rohitchouhan.classroom.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Autowired
    private Utilities utilities;

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

    @Override
    public List<Assignment> getAssignments() {
        try {
            logger.info("Fetching all assignments from cache...");
            List<Assignment> cachedAssignments = (List<Assignment>) redisTemplate.opsForValue().get(ASSIGNMENT_KEY);

            if (cachedAssignments != null) {
                logger.info("Fetched " + cachedAssignments.size() + " assignments from cache");
                return cachedAssignments;
            }

            logger.info("Cache miss, fetching assignments from the database...");
            List<Assignment> assignments = assignmentRepository.findAll();
            logger.info("Fetched " + assignments.size() + " assignments from the database");

            if (assignments != null && !assignments.isEmpty()) {
                // Cache the data in Redis for future requests
                redisTemplate.opsForValue().set(ASSIGNMENT_KEY, assignments);
                redisTemplate.expire(ASSIGNMENT_KEY, cacheExpirationMinutes, TimeUnit.MINUTES);
            }

            return assignments;
        } catch (RedisConnectionFailureException e) {
            logger.error("Error while connecting to Redis server: " + e.getMessage(), e);
            logger.info("Fetching assignments from the database...");
            List<Assignment> assignments = assignmentRepository.findAll();
            if(assignments != null) return assignments;
            return null;
        } catch (Exception e) {
            logger.error("Error while getting assignments: " + e.getMessage(), e);
            // Handle other exceptions as needed
            throw new RuntimeException("Error while getting assignments", e);
        }
    }

    @Override
    public Assignment saveAssignment(Assignment assignment) {
        try {
            logger.info("Saving assignment...");
            Assignment savedAssignment = assignmentRepository.save(assignment);
            logger.info("Saved assignment with ID: " + savedAssignment.getId());
            return savedAssignment;
        } catch (Exception e) {
            logger.error("Error while saving assignment: " + e.getMessage(), e);
            throw new RuntimeException("Error while saving assignment", e);
        }
    }

    @Override
    public Assignment createAssignment(Assignment newAssignment) {
        try {
            logger.info("Creating a new assignment...");
            Assignment createdAssignment = assignmentRepository.save(newAssignment);
            logger.info("Created assignment with ID: " + createdAssignment.getId());
            try {
                sendBulkEmailToAllStudents(newAssignment);
            } catch (RuntimeException e) {
                logger.info("Error in finding students");
            }
            return createdAssignment;
        } catch (Exception e) {
            logger.error("Error while creating assignment: " + e.getMessage(), e);
            throw new RuntimeException("Error while creating assignment", e);
        }
    }

    @Override
    public boolean deleteAssignment(Long id) {
        try {
            assignmentRepository.deleteById(id);
            logger.info("Assignment deleted with ID: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error while deleting assignment with ID: " + id, e);
            return false;
        }
    }

    @Override
    public Optional<Assignment> getAssignmentById(Long id) {
        try {
            logger.info("Fetching assignment by ID: " + id);
            Optional<Assignment> assignmentOptional = assignmentRepository.findById(id);
            if (assignmentOptional.isPresent()) {
                logger.info("Fetched assignment with ID: " + id);
            } else {
                logger.info("No assignment found with ID: " + id);
            }
            return assignmentOptional;
        } catch (Exception e) {
            logger.error("Error while getting assignment by ID: " + e.getMessage(), e);
            throw new RuntimeException("Error while getting assignment by ID", e);
        }
    }

    @Override
    public List<Assignment> getAssignmentsByUserId(Long userId) {
        return null;
    }

    @Override
    public List<Assignment> findAllByCreatedBy(Long userId) {
        try {
            logger.info("Fetching assignments by user ID: " + userId);
            List<Assignment> assignments = assignmentRepository.findAllByCreatedBy(userId);
            logger.info("Fetched " + assignments.size() + " assignments for user ID: " + userId);
            return assignments;
        } catch (Exception e) {
            logger.error("Error while getting assignments by student ID: " + e.getMessage(), e);
            throw new RuntimeException("Error while getting assignments by student ID", e);
        }
    }

    @Override
    public Assignment updateAssignment(Long assignmentId, Assignment updatedAssignment) {
        try {
            logger.info("Updating assignment with ID: " + assignmentId);
            Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);

            if (assignmentOptional.isPresent()) {
                Assignment existingAssignment = assignmentOptional.get();

                if (updatedAssignment.getAssignmentName() != null) {
                    existingAssignment.setAssignmentName(updatedAssignment.getAssignmentName());
                }

                if (updatedAssignment.getDescription() != null) {
                    existingAssignment.setDescription(updatedAssignment.getDescription());
                }

                if (updatedAssignment.getDueDate() != null) {
                    existingAssignment.setDueDate(updatedAssignment.getDueDate());
                }

                updatedAssignment = assignmentRepository.save(existingAssignment);
                logger.info("Updated assignment with ID: " + assignmentId);
                return updatedAssignment;
            } else {
                logger.warn("No assignment found with ID: " + assignmentId);
                throw new ResolutionException("Assignment not found with ID: " + assignmentId);
            }
        } catch (Exception e) {
            logger.error("Error while updating assignment: " + e.getMessage(), e);
            throw new RuntimeException("Error while updating assignment", e);
        }
    }

    @Async
    private void sendBulkEmailToAllStudents(Assignment newAssignment) {
        List<String> studentEmails = null;

        studentEmails = utilities.getAllStudentEmails();

        if (studentEmails != null && studentEmails.size() > 0) {
            String assignmentDetailsEmailBody = utilities.createAssignmentEmailBody(newAssignment);
            emailService.sendBulkEmails(studentEmails, newAssignment.getAssignmentName(), assignmentDetailsEmailBody);
        } else {
            logger.info("No student emails found to send the assignment email.");
        }
    }

}
