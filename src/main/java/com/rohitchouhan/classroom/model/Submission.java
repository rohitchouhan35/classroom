package com.rohitchouhan.classroom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String answer;
    private int score;
    private Date submissionDate;
    private String studentEmail;

    @Column(name = "assignment_id")
    private long assignmentId;

    @Column(name = "user_id")
    private long userId;

}
