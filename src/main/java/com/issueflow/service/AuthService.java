package com.issueflow.service;

import com.issueflow.dto.request.LoginRequest;
import com.issueflow.dto.request.RegisterRequest;
import com.issueflow.dto.response.AuthResponse;
import com.issueflow.dto.response.UserResponse;
import com.issueflow.entity.User;
import com.issueflow.exception.BadRequestException;
import com.issueflow.repository.UserRepository;
import com.issueflow.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtProvider;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        
        String token = jwtProvider.generateToken(auth);
        long expiresInSeconds = jwtExpirationInMs / 1000;

        return new AuthResponse(token, expiresInSeconds);
    }

    public UserResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setRole(req.getRole());

        return UserResponse.from(userRepo.save(user));
    }
}
