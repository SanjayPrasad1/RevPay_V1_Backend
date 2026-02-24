package com.revpay.controller;

import com.revpay.common.ApiResponse;
import com.revpay.dto.moneyrequest.MoneyRequestDto;
import com.revpay.dto.moneyrequest.MoneyRequestResponse;
import com.revpay.service.MoneyRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/money-requests")
public class MoneyRequestController {

    private final MoneyRequestService moneyRequestService;

    public MoneyRequestController(MoneyRequestService moneyRequestService) {
        this.moneyRequestService = moneyRequestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MoneyRequestResponse>> sendRequest(
            @Valid @RequestBody MoneyRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Money request sent",
                moneyRequestService.sendRequest(userDetails.getUsername(), dto)));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<MoneyRequestResponse>> accept(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Request accepted",
                moneyRequestService.acceptRequest(userDetails.getUsername(), id)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<MoneyRequestResponse>> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Request rejected",
                moneyRequestService.rejectRequest(userDetails.getUsername(), id)));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<MoneyRequestResponse>>> sent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Sent requests",
                moneyRequestService.getSentRequests(userDetails.getUsername(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<MoneyRequestResponse>>> received(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Received requests",
                moneyRequestService.getReceivedRequests(userDetails.getUsername(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
}