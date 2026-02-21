package com.firstVersion.RevPay.dto;

import java.math.BigDecimal;

public class LoanRequest {
    private BigDecimal amount;
    private String purpose;
    private Integer tenureMonths;

    // Standard Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
}