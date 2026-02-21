package com.firstVersion.RevPay.service.impl;

import com.firstVersion.RevPay.dto.InvoiceRequest;
import com.firstVersion.RevPay.entity.Invoice;
import com.firstVersion.RevPay.entity.InvoiceItem;
import com.firstVersion.RevPay.entity.User;
import com.firstVersion.RevPay.repository.InvoiceRepository;
import com.firstVersion.RevPay.repository.UserRepository;
import com.firstVersion.RevPay.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private UserRepository userRepository;

    @Override
    @Transactional
    public Invoice createInvoice(String businessEmail, InvoiceRequest request) {
        User business = userRepository.findByEmail(businessEmail)
                .orElseThrow(() -> new RuntimeException("Business user not found"));

        Invoice invoice = new Invoice();
        invoice.setBusiness(business);
        invoice.setCustomerEmail(request.getCustomerEmail());
        invoice.setCustomerName(request.getCustomerName());
        invoice.setDueDate(request.getDueDate());
        invoice.setStatus(Invoice.InvoiceStatus.SENT);

        // Map DTO items to Entity items
        List<InvoiceItem> items = request.getItems().stream().map(dto -> {
            InvoiceItem item = new InvoiceItem();
            item.setDescription(dto.getDescription());
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(dto.getUnitPrice());
            return item;
        }).collect(Collectors.toList());

        invoice.setItems(items);

        // Calculate Total
        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setTotalAmount(total);

        return invoiceRepository.save(invoice);
    }

    @Override
    public List<Invoice> getBusinessInvoices(String businessEmail) {
        User business = userRepository.findByEmail(businessEmail).orElseThrow();
        return invoiceRepository.findByBusiness(business);
    }

    @Override
    @Transactional
    public void markAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
    }
}