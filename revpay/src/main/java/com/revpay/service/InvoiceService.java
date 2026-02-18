package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.invoice.*;
import com.revpay.entity.*;
import com.revpay.enums.InvoiceStatus;
import com.revpay.enums.TransactionStatus;
import com.revpay.enums.TransactionType;
import com.revpay.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceItemRepository invoiceItemRepository,
                          UserRepository userRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // ── Create Invoice ───────────────────────────────────────

    @Transactional
    public InvoiceResponse createInvoice(String email, CreateInvoiceRequest req) {
        User issuer = getUser(email);
        User recipient = userRepository.findByEmail(req.getRecipientEmail())
                .orElseThrow(() -> RevPayException.notFound(
                        "No user found with email: " + req.getRecipientEmail()));

        if (issuer.getId().equals(recipient.getId())) {
            throw RevPayException.badRequest("Cannot create invoice for yourself");
        }

        BigDecimal subtotal = req.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxRate = req.getTaxRate() != null ? req.getTaxRate() : BigDecimal.ZERO;
        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
        BigDecimal total = subtotal.add(taxAmount);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase());
        invoice.setIssuer(issuer);
        invoice.setRecipient(recipient);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setSubtotal(subtotal);
        invoice.setTaxRate(taxRate);
        invoice.setTotalAmount(total);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setCurrency(req.getCurrency() != null ? req.getCurrency() : "USD");
        invoice.setIssueDate(req.getIssueDate());
        invoice.setDueDate(req.getDueDate());
        invoice.setNotes(req.getNotes());

        Invoice saved = invoiceRepository.save(invoice);

        List<InvoiceItem> items = req.getItems().stream().map(itemReq -> {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(saved);
            item.setDescription(itemReq.getDescription());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setLineTotal(itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            return item;
        }).toList();

        invoiceItemRepository.saveAll(items);
        saved.setItems(items);

        return toResponse(saved);
    }

    // ── Send Invoice ─────────────────────────────────────────

    @Transactional
    public InvoiceResponse sendInvoice(String email, Long invoiceId) {
        User issuer = getUser(email);
        Invoice invoice = getInvoice(invoiceId);

        if (!invoice.getIssuer().getId().equals(issuer.getId())) {
            throw RevPayException.forbidden("Not authorized");
        }
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw RevPayException.badRequest("Only DRAFT invoices can be sent");
        }

        invoice.setStatus(InvoiceStatus.SENT);
        return toResponse(invoiceRepository.save(invoice));
    }

    // ── Pay Invoice ──────────────────────────────────────────

    @Transactional
    public InvoiceResponse payInvoice(String email, Long invoiceId) {
        User payer = getUser(email);
        Invoice invoice = getInvoice(invoiceId);

        if (!invoice.getRecipient().getId().equals(payer.getId())) {
            throw RevPayException.forbidden("Not authorized to pay this invoice");
        }
        if (invoice.getStatus() != InvoiceStatus.SENT) {
            throw RevPayException.badRequest("Invoice is not payable");
        }

        Account payerAccount = getPrimaryAccount(payer);
        Account issuerAccount = getPrimaryAccount(invoice.getIssuer());

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getAmountPaid());
        if (payerAccount.getBalance().compareTo(remaining) < 0) {
            throw RevPayException.badRequest("Insufficient balance");
        }

        payerAccount.setBalance(payerAccount.getBalance().subtract(remaining));
        issuerAccount.setBalance(issuerAccount.getBalance().add(remaining));
        accountRepository.save(payerAccount);
        accountRepository.save(issuerAccount);

        Transaction tx = new Transaction();
        tx.setReferenceNumber("TXN" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase());
        tx.setSenderAccount(payerAccount);
        tx.setReceiverAccount(issuerAccount);
        tx.setAmount(remaining);
        tx.setFee(BigDecimal.ZERO);
        tx.setCurrency(invoice.getCurrency());
        tx.setType(TransactionType.INVOICE_PAYMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setCompletedAt(Instant.now());
        tx.setDescription("Payment for invoice " + invoice.getInvoiceNumber());
        transactionRepository.save(tx);

        invoice.setAmountPaid(invoice.getTotalAmount());
        invoice.setStatus(InvoiceStatus.PAID);
        return toResponse(invoiceRepository.save(invoice));
    }

    // dispute invoice
    @Transactional
    public InvoiceResponse disputeInvoice(String email, Long invoiceId, String reason) {
        User recipient = getUser(email);
        Invoice invoice = getInvoice(invoiceId);

        if (!invoice.getRecipient().getId().equals(recipient.getId())) {
            throw RevPayException.forbidden("Not authorized");
        }
        if (invoice.getStatus() != InvoiceStatus.SENT) {
            throw RevPayException.badRequest("Only SENT invoices can be disputed");
        }

        invoice.setStatus(InvoiceStatus.DISPUTED);
        invoice.setDisputeReason(reason);
        return toResponse(invoiceRepository.save(invoice));
    }

    // ── List ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getMyInvoices(String email, Pageable pageable) {
        User user = getUser(email);
        return invoiceRepository.findByIssuerId(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getReceivedInvoices(String email, Pageable pageable) {
        User user = getUser(email);
        return invoiceRepository.findByRecipientId(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(String email, Long id) {
        getUser(email);
        return toResponse(getInvoice(id));
    }

    // ── Helpers ──────────────────────────────────────────────

    private Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> RevPayException.notFound("Invoice not found"));
    }

    private Account getPrimaryAccount(User user) {
        return accountRepository.findByUserId(user.getId())
                .stream().findFirst()
                .orElseThrow(() -> RevPayException.notFound("No account found"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> RevPayException.notFound("User not found"));
    }

    public InvoiceResponse toResponse(Invoice inv) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(inv.getId());
        r.setInvoiceNumber(inv.getInvoiceNumber());
        r.setStatus(inv.getStatus());
        r.setSubtotal(inv.getSubtotal());
        r.setTaxRate(inv.getTaxRate());
        r.setTotalAmount(inv.getTotalAmount());
        r.setAmountPaid(inv.getAmountPaid());
        r.setCurrency(inv.getCurrency());
        r.setIssueDate(inv.getIssueDate());
        r.setDueDate(inv.getDueDate());
        r.setNotes(inv.getNotes());
        r.setIssuerId(inv.getIssuer().getId());
        r.setIssuerName(inv.getIssuer().getFullName());
        r.setIssuerEmail(inv.getIssuer().getEmail());
        r.setRecipientId(inv.getRecipient().getId());
        r.setRecipientName(inv.getRecipient().getFullName());
        r.setRecipientEmail(inv.getRecipient().getEmail());
        r.setCreatedAt(inv.getCreatedAt());
        r.setDisputeReason(inv.getDisputeReason());

        if (inv.getItems() != null) {
            r.setItems(inv.getItems().stream().map(item -> {
                InvoiceItemResponse ir = new InvoiceItemResponse();
                ir.setId(item.getId());
                ir.setDescription(item.getDescription());
                ir.setQuantity(item.getQuantity());
                ir.setUnitPrice(item.getUnitPrice());
                ir.setLineTotal(item.getLineTotal());
                return ir;
            }).toList());
        }
        return r;
    }
    @Transactional
    public InvoiceResponse cancelInvoice(String email, Long invoiceId) {
        User issuer = getUser(email);
        Invoice invoice = getInvoice(invoiceId);

        if (!invoice.getIssuer().getId().equals(issuer.getId())) {
            throw RevPayException.forbidden("Not authorized");
        }
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw RevPayException.badRequest("Only DRAFT invoices can be cancelled");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        return toResponse(invoiceRepository.save(invoice));
    }
}