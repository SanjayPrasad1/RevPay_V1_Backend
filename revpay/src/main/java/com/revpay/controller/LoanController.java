package com.revpay.controller;

import com.revpay.common.ApiResponse;
import com.revpay.dto.loan.ApplyLoanRequest;
import com.revpay.dto.loan.LoanResponse;
import com.revpay.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanResponse>> apply(
            @Valid @RequestBody ApplyLoanRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Loan application submitted",
                loanService.applyForLoan(userDetails.getUsername(), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getMyLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Loans",
                loanService.getMyLoans(userDetails.getUsername(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Loan",
                loanService.getLoanById(userDetails.getUsername(), id)));
    }

    @PostMapping("/{loanId}/emis/{emiId}/repay")
    public ResponseEntity<ApiResponse<LoanResponse>> repayEmi(
            @PathVariable Long loanId,
            @PathVariable Long emiId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("EMI paid",
                loanService.repayEmi(userDetails.getUsername(), loanId, emiId)));
    }
}