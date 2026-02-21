package com.firstVersion.RevPay.service;

import com.firstVersion.RevPay.dto.InvoiceRequest;
import com.firstVersion.RevPay.entity.Invoice;
import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(String businessEmail, InvoiceRequest request);
    List<Invoice> getBusinessInvoices(String businessEmail);
    void markAsPaid(Long invoiceId);
}