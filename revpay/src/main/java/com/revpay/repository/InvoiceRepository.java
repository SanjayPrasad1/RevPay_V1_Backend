package com.revpay.repository;

import com.revpay.entity.Invoice;
import com.revpay.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByIssuerId(Long issuerId, Pageable pageable);
    Page<Invoice> findByRecipientId(Long recipientId, Pageable pageable);
    Page<Invoice> findByIssuerIdAndStatus(Long issuerId, InvoiceStatus status, Pageable pageable);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    long countByIssuerIdAndStatus(Long issuerId, InvoiceStatus status);
}