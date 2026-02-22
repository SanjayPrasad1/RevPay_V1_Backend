package com.revpay.dto.dashboard;

import com.revpay.dto.account.AccountResponse;
import com.revpay.dto.transaction.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {
    private String fullName;
    private String email;
    private String role;
    private AccountResponse primaryAccount;
    private BigDecimal totalBalance;
    private long totalTransactions;
    private long pendingMoneyRequests;
    private List<TransactionResponse> recentTransactions;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public AccountResponse getPrimaryAccount() { return primaryAccount; }
    public void setPrimaryAccount(AccountResponse primaryAccount) { this.primaryAccount = primaryAccount; }

    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }

    public long getPendingMoneyRequests() { return pendingMoneyRequests; }
    public void setPendingMoneyRequests(long pendingMoneyRequests) { this.pendingMoneyRequests = pendingMoneyRequests; }

    public List<TransactionResponse> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<TransactionResponse> recentTransactions) { this.recentTransactions = recentTransactions; }
}