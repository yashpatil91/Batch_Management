package com.batchmanagement.backend.service;

import com.batchmanagement.backend.dto.auth.LoginRequest;
import com.batchmanagement.backend.dto.auth.LoginResponse;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.exception.BadRequestException;
import com.batchmanagement.backend.repository.UserRepository;
import com.batchmanagement.backend.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        LoginResponse response = new LoginResponse();

        response.setToken(jwtService.generateToken(user));
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }
}