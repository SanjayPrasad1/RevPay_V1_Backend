package com.firstVersion.RevPay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class TransactionResponse {
    public TransactionResponse(Long id, String senderEmail, String receiverEmail, BigDecimal amount, String type, String status, LocalDateTime timestamp, String note) {
        this.id = id;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
        this.note = note;
    }

    public TransactionResponse() {
    }

    private Long id;
    private String senderEmail;
    private String receiverEmail;
    private BigDecimal amount;
    private String type;
    private String status;
    private LocalDateTime timestamp;
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}