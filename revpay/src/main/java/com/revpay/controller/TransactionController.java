package com.revpay.controller;

import com.revpay.common.ApiResponse;
import com.revpay.dto.transaction.TopUpRequest;
import com.revpay.dto.transaction.TransactionResponse;
import com.revpay.dto.transaction.TransferRequest;
import com.revpay.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Transfer successful",
                transactionService.transfer(userDetails.getUsername(), request)));
    }

    @PostMapping("/top-up")
    public ResponseEntity<ApiResponse<TransactionResponse>> topUp(
            @Valid @RequestBody TopUpRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Top-up successful",
                transactionService.topUp(userDetails.getUsername(), request)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Page<TransactionResponse> result = transactionService.getHistory(
                userDetails.getUsername(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok("Transaction history", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Transactions",
                transactionService.getTransactions(
                        userDetails.getUsername(),
                        PageRequest.of(page, size,
                                Sort.by("createdAt").descending()))));
    }

    @GetMapping("/export")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> exportTransactions(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok("Transactions",
                transactionService.getTransactionsForExport(
                        userDetails.getUsername(), fromDate, toDate)));
    }
}