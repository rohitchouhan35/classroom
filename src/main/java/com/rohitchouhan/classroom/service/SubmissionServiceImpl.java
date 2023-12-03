package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.dto.GradingData;
import com.rohitchouhan.classroom.dto.StudentInfo;
import com.rohitchouhan.classroom.exception.DuplicateEntityException;
import com.rohitchouhan.classroom.model.Submission;
import com.rohitchouhan.classroom.repository.SubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImpl.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public List<Submission> getSubmissions() {
        return submissionRepository.findAll();
    }

    @Override
    public List<StudentInfo> getAllStudentWithAssignmentId(Long assignmentId) {
        List<StudentInfo> studentInfoList = new ArrayList<>();

        try {
            List<Submission> studentsWithAssignmentId = submissionRepository.findByAssignmentId(assignmentId);
            for (Submission studentSubmission : studentsWithAssignmentId) {
                StudentInfo studentInfo = new StudentInfo();
                studentInfo.setScore(studentSubmission.getScore());
                studentInfo.setStudentId(studentSubmission.getUserId());
                studentInfoList.add(studentInfo);
            }
            logger.info("Fetched student information for assignment ID: {}", assignmentId);
        } catch (Exception e) {
            logger.error("Error while fetching student information for assignment ID: {}", assignmentId, e);
            return null;
        }

        return studentInfoList;
    }


    @Override
    public Submission getSubmissionById(Long id) {
        Optional<Submission> submissionOptional = submissionRepository.findById(id);
        if (submissionOptional.isPresent()) {
            return submissionOptional.get();
        } else {
            return null;
        }
    }

    @Override
    public Submission createSubmission(Long userId, Long assignmentId, Submission submission) {
        try {
            Optional<Submission> existingSubmission = submissionRepository.findByAssignmentIdAndUserId(assignmentId, userId);
            if (existingSubmission.isPresent()) {
                throw new DuplicateEntityException("Assignment has already been submitted.");
            }

            LocalDate now = LocalDate.now();
            submission.setSubmissionDate(Date.valueOf(now));
            submission.setScore(0);
            submission.setUserId(userId);
            submission.setAssignmentId(assignmentId);
            Submission createdSubmission = submissionRepository.save(submission);
            return createdSubmission;
        } catch (DuplicateEntityException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error while creating submission: " + e.getMessage(), e);
            throw new RuntimeException("Error while creating submission", e);
        }
    }

    @Override
    public Submission gradeSubmission(GradingData gradingData) {
        try {
            Optional<Submission> submissionOptional = submissionRepository.findById(gradingData.getSubmissionId());

            if (submissionOptional.isPresent()) {
                Submission submission = submissionOptional.get();
                submission.setScore(gradingData.getScore());
                Submission updatedSubmission = submissionRepository.save(submission);
                if(submission.getStudentEmail() != null && !submission.getStudentEmail().isEmpty()) {
                    logger.info("Sending notification to student with ID: {}", submission.getUserId());
                    emailService.sendEmail(submission.getStudentEmail(), "Review", "Your assignment with ID" + submission.getAssignmentId() + " has been reviewed");
                };
                return updatedSubmission;
            } else {
                throw new EntityNotFoundException("Submission not found.");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error while updating submission: " + e.getMessage(), e);
            throw new RuntimeException("Error while updating submission", e);
        }
    }

    @Override
    public List<Submission> getSubmissionByStudentId(Long studentId) {
        return submissionRepository.findAllByUserId(studentId);
    }

}
