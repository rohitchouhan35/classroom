package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.dto.GradingData;
import com.rohitchouhan.classroom.dto.StudentInfo;
import com.rohitchouhan.classroom.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionService {

    List<Submission> getSubmissions();
    List<StudentInfo> getAllStudentWithAssignmentId(Long assignmentId);
    Submission getSubmissionById(Long id);
    Submission createSubmission(Long userId, Long assignmentId, Submission submission);
    Submission gradeSubmission(GradingData gradingData);

    List<Submission> getSubmissionByStudentId(Long studentId);
}
