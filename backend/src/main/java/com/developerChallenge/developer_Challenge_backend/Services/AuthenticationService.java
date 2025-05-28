package com.developerChallenge.developer_Challenge_backend.Services;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.developerChallenge.developer_Challenge_backend.DTOs.AuthenticationResponse;
import com.developerChallenge.developer_Challenge_backend.Models.Role;
import com.developerChallenge.developer_Challenge_backend.Models.User;
import com.developerChallenge.developer_Challenge_backend.Repositories.UserRepository;


@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public AuthenticationService(
        AuthenticationManager authenticationManager,
        UserRepository repository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(User request) {
        User user = new User();

        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign USER role if not provided
        if (request.getRole() == null) {
            user.setRole(Role.TEACHER);
        } else {
            user.setRole(request.getRole());
        }

        user = repository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthenticationResponse(token, user.getRole().name());
    }
    public AuthenticationResponse authenticate(User request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        User user = repository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);

        return new AuthenticationResponse(token, user.getRole().name());

}
    public AuthenticationResponse registerEmployeeWithNotification(User request) {
        User user = new User();

        String rawPassword = generateRandomPassword(8); // plain password for email
        String encodedPassword = passwordEncoder.encode(rawPassword);

        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(encodedPassword);

        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role must be provided");
        }

        user.setRole(request.getRole());

        user = repository.save(user);

        String token = jwtService.generateToken(user);

      
        return new AuthenticationResponse(token, user.getRole().name());
    }
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

}


