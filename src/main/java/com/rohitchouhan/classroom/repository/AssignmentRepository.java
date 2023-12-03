package com.rohitchouhan.classroom.repository;

import com.rohitchouhan.classroom.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findAllByCreatedBy(Long userId);
    @Query("SELECT a FROM Assignment a WHERE a.dueDate >= CURRENT_DATE")
    List<Assignment> findAssignmentsWithDueDateTodayOrInFuture();

}
