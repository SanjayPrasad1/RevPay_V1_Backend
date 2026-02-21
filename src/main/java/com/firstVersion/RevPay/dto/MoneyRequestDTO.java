package com.firstVersion.RevPay.dto;

import java.math.BigDecimal;

public class MoneyRequestDTO {
    private String targetUserEmail;
    private BigDecimal amount;
    private String purpose;

    public MoneyRequestDTO() {}

    // Standard Getters and Setters
    public String getTargetUserEmail() { return targetUserEmail; }
    public void setTargetUserEmail(String targetUserEmail) { this.targetUserEmail = targetUserEmail; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}