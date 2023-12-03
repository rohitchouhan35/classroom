package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.model.Assignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    List<Assignment> getAssignments();

    Assignment saveAssignment(Assignment assignment);

    Optional<Assignment> getAssignmentById(Long id);

    List<Assignment> getAssignmentsByUserId(Long userId);

    List<Assignment> findAllByCreatedBy(Long userId);

    Assignment updateAssignment(Long assignmentId, Assignment updatedAssignment);

    Assignment createAssignment(Assignment newAssignment);

    boolean deleteAssignment(Long id);
}
