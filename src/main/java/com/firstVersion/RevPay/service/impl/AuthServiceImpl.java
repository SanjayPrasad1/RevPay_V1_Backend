package com.firstVersion.RevPay.service.impl;

import com.firstVersion.RevPay.dto.AuthResponse;
import com.firstVersion.RevPay.dto.RegisterRequest;
import com.firstVersion.RevPay.entity.User;
import com.firstVersion.RevPay.repository.UserRepository;
import com.firstVersion.RevPay.service.AuthService;
import com.firstVersion.RevPay.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public AuthResponse authenticateUser(String email, String password) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate the JWT token
        String jwt = jwtUtils.generateToken(authentication.getName());

        // Fetch user to get their role/type
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return the DTO using the constructor (No casting needed)
        return new AuthResponse(
                jwt,
                user.getEmail(),
                user.getUserType().name()
        );
    }

    @Override
    public String registerUser(RegisterRequest info) {
        if (userRepository.existsByEmail(info.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setEmail(info.getEmail());
        user.setFullName(info.getFullName());
        user.setPhoneNumber(info.getPhoneNumber());
        user.setUserType(info.getUserType());
        user.setPassword(passwordEncoder.encode(info.getPassword()));

        // Financial initialization
        user.setWalletBalance(BigDecimal.ZERO);

        // Map business fields if the user is a business account
        user.setBusinessName(info.getBusinessName());
        user.setTaxId(info.getTaxId());

        userRepository.save(user);
        return "User registered successfully!";
    }
}