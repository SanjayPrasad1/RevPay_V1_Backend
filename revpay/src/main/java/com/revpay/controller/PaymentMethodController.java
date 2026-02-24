package com.revpay.controller;

import com.revpay.common.ApiResponse;
import com.revpay.dto.payment.PaymentMethodRequest;
import com.revpay.dto.payment.PaymentMethodResponse;
import com.revpay.service.PaymentMethodService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments/methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Payment methods",
                paymentMethodService.getMyPaymentMethods(userDetails.getUsername())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> add(
            @Valid @RequestBody PaymentMethodRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Payment method added",
                paymentMethodService.addPaymentMethod(userDetails.getUsername(), request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        paymentMethodService.deletePaymentMethod(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Payment method removed"));
    }
}