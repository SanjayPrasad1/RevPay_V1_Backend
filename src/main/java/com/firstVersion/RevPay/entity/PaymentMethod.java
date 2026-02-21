package com.firstVersion.RevPay.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_methods")
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String type; // CREDIT_CARD, DEBIT_CARD, BANK_ACCOUNT

    public PaymentMethod() {
    }

    private String provider; // e.g., Visa, Mastercard, Chase

    private String accountMask; // e.g., **** **** **** 1234

    public PaymentMethod(Long id, User user, String type, String provider, String accountMask, String expiryDate, boolean isDefault) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.provider = provider;
        this.accountMask = accountMask;
        this.expiryDate = expiryDate;
        this.isDefault = isDefault;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAccountMask() {
        return accountMask;
    }

    public void setAccountMask(String accountMask) {
        this.accountMask = accountMask;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    private String expiryDate; // MM/YY

    private boolean isDefault = false;

    // Security Note: Never store CVV or full card numbers in your DB for PCI compliance.
    // In a real app, you'd store a 'Token' provided by Stripe or PayPal.
}