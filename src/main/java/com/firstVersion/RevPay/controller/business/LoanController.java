package com.firstVersion.RevPay.controller.business;

import com.firstVersion.RevPay.dto.LoanRequest;
import com.firstVersion.RevPay.entity.Loan;
import com.firstVersion.RevPay.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business/loans")
@PreAuthorize("hasAuthority('BUSINESS')") // Ensures only business users can access
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<Loan> applyForLoan(Authentication authentication, @RequestBody LoanRequest request) {
        // The email is extracted from the JWT token
        String businessEmail = authentication.getName();
        Loan loan = loanService.applyForLoan(businessEmail, request);
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/my-loans")
    public ResponseEntity<List<Loan>> getMyLoans(Authentication authentication) {
        String businessEmail = authentication.getName();
        return ResponseEntity.ok(loanService.getMyLoans(businessEmail));
    }
}