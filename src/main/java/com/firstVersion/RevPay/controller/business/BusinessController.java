package com.firstVersion.RevPay.controller.business;

import com.firstVersion.RevPay.dto.InvoiceRequest;
import com.firstVersion.RevPay.entity.Invoice;
import com.firstVersion.RevPay.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@PreAuthorize("hasAuthority('BUSINESS')")
public class BusinessController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/invoices")
    public ResponseEntity<Invoice> createInvoice(Authentication auth, @RequestBody InvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.createInvoice(auth.getName(), request));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getMyInvoices(Authentication auth) {
        return ResponseEntity.ok(invoiceService.getBusinessInvoices(auth.getName()));
    }
}