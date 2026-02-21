package com.firstVersion.RevPay.service;

import com.firstVersion.RevPay.dto.AuthResponse;
import com.firstVersion.RevPay.dto.RegisterRequest;

public interface AuthService {
    // Change 'User user' to 'RegisterRequest registration'
    String registerUser(RegisterRequest registration);

    // Change the return type from Map to the AuthResponse DTO
    AuthResponse authenticateUser(String email, String password);
}