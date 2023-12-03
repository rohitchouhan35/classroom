package com.rohitchouhan.classroom.controller;

import com.rohitchouhan.classroom.model.Assignment;
import com.rohitchouhan.classroom.service.AssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Assignment>>> getAssignments() {
        try {
            List<Assignment> assignments = assignmentService.getAssignments();
            return new ResponseEntity<>(new ApiResponse<>("success", "Assignments fetched successfully", assignments), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while fetching assignments", e);
            return new ResponseEntity<>(new ApiResponse<>("error", "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getAssignmentById(@PathVariable Long id) {
        try {
            Optional<Assignment> assignment = assignmentService.getAssignmentById(id);
            if (assignment.isPresent()) {
                return new ResponseEntity<>(new ApiResponse<>("success", "Assignment fetched successfully", assignment.get()), HttpStatus.OK);
            } else {
                String errorMessage = "Assignment with ID " + id + " not found";
                logger.warn(errorMessage);
                return new ResponseEntity<>(new ApiResponse<>("error", errorMessage, null), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error while fetching assignment by ID", e);
            return new ResponseEntity<>(new ApiResponse<>("error", "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/protected/save")
    public ResponseEntity<ApiResponse<?>> createAssignment(@RequestBody Assignment assignment) {
        try {
            Assignment savedAssignment = assignmentService.createAssignment(assignment);
            if (savedAssignment != null) {
                logger.info("Assignment created successfully with ID: {}", savedAssignment.getId());
                return new ResponseEntity<>(new ApiResponse<>("success", "Assignment created successfully", savedAssignment), HttpStatus.CREATED);
            } else {
                String errorMessage = "Error while creating assignment";
                logger.error(errorMessage);
                return new ResponseEntity<>(new ApiResponse<>("error", errorMessage, null), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            String errorMessage = "Error while creating assignment: " + e.getMessage();
            logger.error(errorMessage, e);
            return new ResponseEntity<>(new ApiResponse<>("error", errorMessage, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/protected/update")
    public ResponseEntity<ApiResponse<?>> updateAssignment(@RequestBody Assignment assignment) {
        try {
            Assignment updatedAssignment = assignmentService.updateAssignment(assignment.getId(), assignment);
            if(updatedAssignment != null) {
                logger.info("Assignment updated successfully with ID: {}", updatedAssignment.getId());
                return new ResponseEntity<>(new ApiResponse<>("success", "Assignment updated successfully", updatedAssignment), HttpStatus.OK);
            } else {
                String errorMessage = "Error while saving assignment";
                logger.error(errorMessage);
                return new ResponseEntity<>(new ApiResponse<>("error", errorMessage, null), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            String errorMessage = "Error while saving assignment: " + e.getMessage();
            logger.error(errorMessage, e);
            return new ResponseEntity<>(new ApiResponse<>("error", errorMessage, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/protected/delete/{id}")
    public ResponseEntity<ApiResponse<?>> deleteAssignment(@PathVariable Long id) {
        try {
            boolean deleted = assignmentService.deleteAssignment(id);
            if (deleted) {
                logger.info("Assignment deleted successfully with ID: {}", id);
                return new ResponseEntity<>(new ApiResponse<>("success", "Assignment deleted successfully", null), HttpStatus.OK);
            } else {
                String errorMessage = "Error while deleting assignment";
                logger.error(errorMessage);
                return new ResponseEntity<>(new ApiResponse<>("error", errorMessage, null), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            String errorMessage = "Error while deleting assignment: " + e.getMessage();
            logger.error(errorMessage, e);
            return new ResponseEntity<>(new ApiResponse<>("error", errorMessage, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
