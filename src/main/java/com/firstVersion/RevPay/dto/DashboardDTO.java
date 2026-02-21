package com.firstVersion.RevPay.dto;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardDTO {
    private BigDecimal currentBalance;
    private BigDecimal totalSpentMonth;
    private BigDecimal totalReceivedMonth;
    private long pendingRequestsCount;

    public DashboardDTO() {}

    // Getters and Setters
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public BigDecimal getTotalSpentMonth() { return totalSpentMonth; }
    public void setTotalSpentMonth(BigDecimal totalSpentMonth) { this.totalSpentMonth = totalSpentMonth; }
    public BigDecimal getTotalReceivedMonth() { return totalReceivedMonth; }
    public void setTotalReceivedMonth(BigDecimal totalReceivedMonth) { this.totalReceivedMonth = totalReceivedMonth; }
    public long getPendingRequestsCount() { return pendingRequestsCount; }
    public void setPendingRequestsCount(long pendingRequestsCount) { this.pendingRequestsCount = pendingRequestsCount; }
}