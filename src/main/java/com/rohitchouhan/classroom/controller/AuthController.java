package com.rohitchouhan.classroom.controller;

import com.rohitchouhan.classroom.dto.AuthResponse;
import com.rohitchouhan.classroom.dto.LoginRequest;
import com.rohitchouhan.classroom.dto.SignUpRequest;
import com.rohitchouhan.classroom.exception.DuplicateEntityException;
import com.rohitchouhan.classroom.model.User;
import com.rohitchouhan.classroom.security.TokenProvider;
import com.rohitchouhan.classroom.security.WebSecurityConfig;
import com.rohitchouhan.classroom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @GetMapping("/isTokenValid")
    public ResponseEntity<ApiResponse<?>> isUserValid(@RequestHeader("Authorization") String token) {
        log.info("Checking if the user with token is valid in isUserValid");

        try {
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("error", "Token is null", null));
            }

            if (tokenProvider.validateTokenAndGetJws(token).isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>("success", "User is valid", null));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("error", "Invalid token", null));
            }
        } catch (Exception e) {
            log.error("Error validating token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("error", "Invalid token", null));
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest) {
        log.info("Received login request for user: {}", loginRequest.getUsername());

        try {
            String token = authenticateAndGetToken(loginRequest.getUsername(), loginRequest.getPassword());
            log.info("User {} authenticated successfully.", loginRequest.getUsername());
            return ResponseEntity.ok(new ApiResponse<>("success", "User authenticated successfully", new AuthResponse(loginRequest.getUsername(), token)));
        } catch (BadCredentialsException ex) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("error", "Bad credentials", null));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody SignUpRequest signUpRequest) {
        log.info("Received signup request for user: {}", signUpRequest.getUsername());

        if (userService.hasUserWithUsername(signUpRequest.getUsername())) {
            log.error("Username {} is already in use.", signUpRequest.getUsername());
            return new ResponseEntity<>(new ApiResponse<>("error", "Username is already in use", null), HttpStatus.BAD_REQUEST);
        }

        if (userService.hasUserWithEmail(signUpRequest.getEmail())) {
            log.error("Email {} is already in use.", signUpRequest.getEmail());
            return new ResponseEntity<>(new ApiResponse<>("error", "Email is already in use", null), HttpStatus.BAD_REQUEST);
        }

        User user = null;
        if(signUpRequest.getRole().equals("teacher")) {
            user = mapSignUpRequestToAdmin(signUpRequest);
        } else {
            user = mapSignUpRequestToUser(signUpRequest);
        }

        userService.saveUser(user);

        log.info("User {} registered successfully.", signUpRequest.getUsername());
        String token = authenticateAndGetToken(signUpRequest.getUsername(), signUpRequest.getPassword());
        return ResponseEntity.ok(new ApiResponse<>("success", "User registered and authenticated successfully", new AuthResponse(signUpRequest.getUsername(), token)));
    }


    private String authenticateAndGetToken(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return tokenProvider.generate(authentication);
    }

    private User mapSignUpRequestToUser(SignUpRequest signUpRequest) {
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setRole(WebSecurityConfig.USER);
        return user;
    }

    private User mapSignUpRequestToAdmin(SignUpRequest signUpRequest) {
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setRole(WebSecurityConfig.ADMIN);
        return user;
    }
}
