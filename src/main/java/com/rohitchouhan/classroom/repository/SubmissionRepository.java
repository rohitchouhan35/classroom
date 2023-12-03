package com.rohitchouhan.classroom.repository;

import com.rohitchouhan.classroom.model.Submission;
import com.rohitchouhan.classroom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByUserId(Long studentId);
    Optional<Submission> findByAssignmentIdAndUserId(Long assignmentId, Long userId);
    List<Submission> findByAssignmentId(Long assignmentId);

    List<Submission> findAllByUserId(Long studentId);
}
