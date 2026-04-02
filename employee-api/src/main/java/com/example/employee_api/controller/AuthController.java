package com.example.employee_api.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.employee_api.model.AuthRequest;
import com.example.employee_api.model.AuthResponse;
import com.example.employee_api.model.LogoutRequest;
import com.example.employee_api.model.RefreshTokenRequest;
import com.example.employee_api.model.RegisterRequest;
import com.example.employee_api.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public java.util.Map<String, Object> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return java.util.Map.of("message", "Refresh token blacklisted successfully");
    }
}
