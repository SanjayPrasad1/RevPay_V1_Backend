package com.firstVersion.RevPay.controller.auth;

import com.firstVersion.RevPay.dto.AuthResponse;
import com.firstVersion.RevPay.dto.RegisterRequest;
import com.firstVersion.RevPay.repository.UserRepository;
import com.firstVersion.RevPay.service.AuthService;
import com.firstVersion.RevPay.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registration) {
        return ResponseEntity.ok(authService.registerUser(registration));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> loginRequest) {
        AuthResponse response = authService.authenticateUser(
                loginRequest.get("email"),
                loginRequest.get("password")
        );
        return ResponseEntity.ok(response);
    }
}