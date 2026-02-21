package com.firstVersion.RevPay.controller.personal;

import com.firstVersion.RevPay.dto.TransactionRequest;
import com.firstVersion.RevPay.dto.TransactionResponse;
import com.firstVersion.RevPay.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/personal/wallet")
public class WalletController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMoney(Authentication authentication, @RequestBody TransactionRequest request) {
        // authentication.getName() retrieves the email of the logged-in user from the JWT
        String senderEmail = authentication.getName();

        try {
            // Now passing the DTO object instead of individual strings/numbers
            TransactionResponse response = transactionService.sendMoney(senderEmail, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add-money")
    public ResponseEntity<?> addMoney(Authentication auth, @RequestParam BigDecimal amount) {
        try {
            transactionService.addMoney(auth.getName(), amount);
            return ResponseEntity.ok("Balance updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(authentication.getName()));
    }
}