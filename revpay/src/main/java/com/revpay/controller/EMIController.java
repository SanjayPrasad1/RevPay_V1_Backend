package com.revpay.controller;

import com.revpay.common.ApiResponse;
import com.revpay.dto.emi.EMIResponse;
import com.revpay.service.EMIService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emis")
public class EMIController {

    private final EMIService emiService;

    public EMIController(EMIService emiService) {
        this.emiService = emiService;
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<ApiResponse<List<EMIResponse>>> getSchedule(
            @PathVariable Long loanId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("EMI schedule",
                emiService.getEmiSchedule(
                        userDetails.getUsername(), loanId)));
    }

    @PostMapping("/{emiId}/pay")
    public ResponseEntity<ApiResponse<EMIResponse>> payEmi(
            @PathVariable Long emiId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("EMI paid",
                emiService.payEmi(userDetails.getUsername(), emiId)));
    }

    @PostMapping("/loan/{loanId}/auto-debit/toggle")
    public ResponseEntity<ApiResponse<String>> toggleAutoDebit(
            @PathVariable Long loanId,
            @AuthenticationPrincipal UserDetails userDetails) {
        emiService.toggleAutoDebit(userDetails.getUsername(), loanId);
        return ResponseEntity.ok(ApiResponse.ok("Auto-debit toggled", "OK"));
    }
}