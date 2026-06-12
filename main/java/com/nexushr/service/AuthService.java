package com.nexushr.service;

import com.nexushr.dto.AuthDto;

public interface AuthService {
    AuthDto.AuthResponse login(AuthDto.LoginRequest request);
    AuthDto.AuthResponse register(AuthDto.RegisterRequest request);
    AuthDto.AuthResponse refresh(AuthDto.RefreshTokenRequest request);
    void logout(String email);
    void changePassword(String email, AuthDto.ChangePasswordRequest request);
    void forgotPassword(AuthDto.ForgotPasswordRequest request);
}
