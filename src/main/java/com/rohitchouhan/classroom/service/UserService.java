package com.rohitchouhan.classroom.service;

import com.rohitchouhan.classroom.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getUsers();
    User saveUser(User user);
    Optional<User> getUserByUsername(String username);
    public List<User> getAllStudents();
    boolean hasUserWithUsername(String username);
    boolean hasUserWithEmail(String email);
}
