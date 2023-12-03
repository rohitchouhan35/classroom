package com.rohitchouhan.classroom.controller;

import com.rohitchouhan.classroom.dto.GradingData;
import com.rohitchouhan.classroom.dto.StudentInfo;
import com.rohitchouhan.classroom.exception.DuplicateEntityException;
import com.rohitchouhan.classroom.model.Submission;
import com.rohitchouhan.classroom.service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/submission")
public class SubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    @Autowired
    private SubmissionService submissionService;

    @GetMapping("/protected")
    public ResponseEntity<ApiResponse<List<Submission>>> getSubmissions() {
        logger.info("Fetching all submissions...");
        try {
            List<Submission> response = submissionService.getSubmissions();
            logger.info("Fetched total " + response.size() + " submissions");
            return new ResponseEntity<>(new ApiResponse<>("success", "Submissions fetched successfully", response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching submissions: " + e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("error", "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/protected/studentWithAssignmentId/{assignmentId}")
    public ResponseEntity<ApiResponse<List<StudentInfo>>> getAllStudentWithAssignmentId(@PathVariable Long assignmentId) {
        logger.info("Fetching all submissions with assignment ID: {}", assignmentId);
        try {
            List<StudentInfo> response = submissionService.getAllStudentWithAssignmentId(assignmentId);
            logger.info("Fetched a total of " + response.size() + " student info records");
            return new ResponseEntity<>(new ApiResponse<>("success", "Student info fetched successfully", response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching student info: " + e.getMessage(), e);
            return new ResponseEntity<>(new ApiResponse<>("error", "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/protected/{studentId}")
    public ResponseEntity<ApiResponse<List<Submission>>> getSubmissionsByStudentId(@PathVariable Long studentId) {
        logger.info("Fetching submissions for student with ID: " + studentId);
        try {
            List<Submission> response = submissionService.getSubmissionByStudentId(studentId);
            logger.info("Fetched submissions for student with ID: " + studentId);
            return new ResponseEntity<>(new ApiResponse<>("success", "Submissions fetched successfully for user with ID: " + studentId, response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching submissions for student: " + e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("error", "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/protected")
    public ResponseEntity<ApiResponse<?>> postSubmission(@RequestBody Submission newSubmission) {
        logger.info("Creating a new submission");
        try {
            Submission submission = submissionService.createSubmission(newSubmission.getUserId(), newSubmission.getAssignmentId(), newSubmission);
            return new ResponseEntity<>(new ApiResponse<>("success", "Submission created successfully", submission), HttpStatus.CREATED);
        } catch (DuplicateEntityException e) {
            return new ResponseEntity<>(new ApiResponse<>("error", "Assignment has already been submitted.", null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating a new submission: " + e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("error", "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/protected/update")
    public ResponseEntity<ApiResponse<?>> gradeSubmission(@RequestBody GradingData gradingData) {
        logger.info("Updating a submission...");
        try {
            Submission submission = submissionService.gradeSubmission(gradingData);
            return new ResponseEntity<>(new ApiResponse<>("success", "Submission updated successfully", submission), HttpStatus.OK);
        } catch (DuplicateEntityException e) {
            return new ResponseEntity<>(new ApiResponse<>("error", "Assignment has already been submitted.", null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating a new submission: " + e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("error", "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
