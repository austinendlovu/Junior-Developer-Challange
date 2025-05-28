package com.developerChallenge.developer_Challenge_backend.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developerChallenge.developer_Challenge_backend.DTOs.AuthenticationResponse;
import com.developerChallenge.developer_Challenge_backend.Models.User;
import com.developerChallenge.developer_Challenge_backend.Repositories.UserRepository;
import com.developerChallenge.developer_Challenge_backend.Services.AuthenticationService;
import com.developerChallenge.developer_Challenge_backend.Services.JwtService;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthenticationController(AuthenticationService authenticationService,
                                    JwtService jwtService,
                                    UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User request) {
        AuthenticationResponse response = authenticationService.register(request);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "You have successfully registered!");
        body.put("authenticationResponse", response);

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User request) {
        AuthenticationResponse response = authenticationService.authenticate(request);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "You have successfully logged in!");
        body.put("authenticationResponse", response);

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
