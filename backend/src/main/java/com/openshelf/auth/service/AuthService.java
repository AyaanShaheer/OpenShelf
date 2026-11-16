package com.openshelf.auth.service;

import com.openshelf.auth.domain.User;
import com.openshelf.auth.dto.AuthResponse;
import com.openshelf.auth.dto.CurrentUserResponse;
import com.openshelf.auth.dto.LoginRequest;
import com.openshelf.auth.repository.UserRepository;
import com.openshelf.auth.security.JwtService;
import com.openshelf.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final long expirationMs;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository,
                       @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.expirationMs = expirationMs;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        if (!user.isActive()) {
            throw new BadRequestException("Account is inactive");
        }
        String token = jwtService.generateToken(principal, Map.of(
                "uid", user.getId(),
                "role", user.getRole().name()
        ));
        return new AuthResponse(
                token,
                "Bearer",
                expirationMs,
                new AuthResponse.UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getRole())
        );
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse me(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return new CurrentUserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.isActive());
    }
}
