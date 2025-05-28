package com.developerChallenge.developer_Challenge_backend.Controller;

import java.util.HashMap;
import java.util.Map;

import com.developerChallenge.developer_Challenge_backend.DTOs.AuthenticationResponse;
import com.developerChallenge.developer_Challenge_backend.Models.User;
import com.developerChallenge.developer_Challenge_backend.Repositories.UserRepository;
import com.developerChallenge.developer_Challenge_backend.Services.AuthenticationService;
import com.developerChallenge.developer_Challenge_backend.Services.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Register a new teacher",
               description = "Creates a new user account and returns a JWT token with user details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User request) {
        AuthenticationResponse response = authenticationService.register(request);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "You have successfully registered!");
        body.put("authenticationResponse", response);

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = "Login as a teacher",
               description = "Authenticates a user and returns a JWT token with user details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User logged in successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Missing or invalid request body")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User request) {
        AuthenticationResponse response = authenticationService.authenticate(request);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "You have successfully logged in!");
        body.put("authenticationResponse", response);

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
