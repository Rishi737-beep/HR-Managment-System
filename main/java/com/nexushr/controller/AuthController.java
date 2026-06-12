package com.nexushr.controller;

import com.nexushr.dto.AuthDto;
import com.nexushr.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication & Authorization", description = "Endpoints for employee registration, login, logout, refresh token, and password management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new employee/user", description = "Creates a new employee profile and returns active JWT authentication tokens")
    public ResponseEntity<AuthDto.AuthResponse> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT tokens", description = "Authenticates user using email and password, and returns access token and refresh token")
    public ResponseEntity<AuthDto.AuthResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token", description = "Generates a new access token and refresh token using a valid, non-expired refresh token")
    public ResponseEntity<AuthDto.AuthResponse> refresh(@Valid @RequestBody AuthDto.RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Log out user", description = "Invalidates the refresh token for the authenticated user")
    public ResponseEntity<Void> logout(Principal principal) {
        authService.logout(principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password", description = "Updates password after validating current password")
    public ResponseEntity<Void> changePassword(Principal principal, @Valid @RequestBody AuthDto.ChangePasswordRequest request) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password request", description = "Triggers a password recovery flow and logs the simulated action")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody AuthDto.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }
}
