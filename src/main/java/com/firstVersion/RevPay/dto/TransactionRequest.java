package com.firstVersion.RevPay.dto;

import java.math.BigDecimal;

public class TransactionRequest {
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public TransactionRequest() {
    }

    public TransactionRequest(String receiverEmail, BigDecimal amount, String note) {
        this.receiverEmail = receiverEmail;
        this.amount = amount;
        this.note = note;
    }

    private String receiverEmail; // The user receiving the funds
    private BigDecimal amount;
    private String note;
}