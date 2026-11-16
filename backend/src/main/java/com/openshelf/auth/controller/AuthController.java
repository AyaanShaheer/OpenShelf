package com.openshelf.auth.controller;

import com.openshelf.auth.dto.AuthResponse;
import com.openshelf.auth.dto.CurrentUserResponse;
import com.openshelf.auth.dto.LoginRequest;
import com.openshelf.auth.service.AuthService;
import com.openshelf.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), "Login successful");
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(Authentication authentication) {
        return ApiResponse.ok(authService.me(authentication.getName()));
    }
}
