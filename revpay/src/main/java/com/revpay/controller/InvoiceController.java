package com.revpay.controller;

import com.revpay.common.ApiResponse;
import com.revpay.dto.invoice.CreateInvoiceRequest;
import com.revpay.dto.invoice.InvoiceResponse;
import com.revpay.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(
            @Valid @RequestBody CreateInvoiceRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Invoice created",
                invoiceService.createInvoice(userDetails.getUsername(), req)));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<InvoiceResponse>> send(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Invoice sent",
                invoiceService.sendInvoice(userDetails.getUsername(), id)));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<InvoiceResponse>> pay(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Invoice paid",
                invoiceService.payInvoice(userDetails.getUsername(), id)));
    }

    @GetMapping("/issued")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> issued(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Issued invoices",
                invoiceService.getMyInvoices(userDetails.getUsername(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> received(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Received invoices",
                invoiceService.getReceivedInvoices(userDetails.getUsername(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Invoice",
                invoiceService.getById(userDetails.getUsername(), id)));
    }

    @PostMapping("/{id}/dispute")
    public ResponseEntity<ApiResponse<InvoiceResponse>> dispute(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String reason = body.getOrDefault("reason", "Disputed by recipient");
        return ResponseEntity.ok(ApiResponse.ok("Invoice disputed",
                invoiceService.disputeInvoice(userDetails.getUsername(), id, reason)));
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Invoice cancelled",
                invoiceService.cancelInvoice(userDetails.getUsername(), id)));
    }
}