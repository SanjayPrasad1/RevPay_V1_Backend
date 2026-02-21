package com.firstVersion.RevPay.controller.personal;

import com.firstVersion.RevPay.dto.MoneyRequestDTO;
import com.firstVersion.RevPay.service.MoneyRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/personal/requests")
public class MoneyRequestController {

    @Autowired
    private MoneyRequestService requestService;

    @PostMapping("/create")
    public ResponseEntity<?> create(Authentication auth, @RequestBody MoneyRequestDTO dto) {
        return ResponseEntity.ok(requestService.createRequest(auth.getName(), dto));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending(Authentication auth) {
        return ResponseEntity.ok(requestService.getMyIncomingRequests(auth.getName()));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id) {
        requestService.acceptRequest(id);
        return ResponseEntity.ok("Request accepted and money transferred.");
    }
}