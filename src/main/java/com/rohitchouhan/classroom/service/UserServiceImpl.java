package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.model.User;
import com.rohitchouhan.classroom.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> getAllStudents() {
        try {
            List<User> students = userRepository.findByRole("student");
            if (students.isEmpty()) {
                log.info("No students found.");
            } else {
                log.info("Retrieved {} students.", students.size());
            }
            return students;
        } catch (RuntimeException e) {
            log.error("Error while fetching students: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<User> getUsers() {
        log.info("Fetching all users.");
        return userRepository.findAll();
    }

    @Override
    public User saveUser(User user) {
        log.info("Saving user: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean hasUserWithUsername(String username) {
        log.info("Checking if user with username exists: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean hasUserWithEmail(String email) {
        log.info("Checking if user with email exists: {}", email);
        return userRepository.existsByEmail(email);
    }

}
