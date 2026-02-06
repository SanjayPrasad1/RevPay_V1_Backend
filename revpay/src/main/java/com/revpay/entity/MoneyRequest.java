package com.revpay.entity;

import com.revpay.enums.MoneyRequestStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "money_requests")
public class MoneyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoneyRequestStatus status = MoneyRequestStatus.PENDING;

    private Instant expiresAt;

    // ── Relationships ────────────────────────────────────────

    /** User who is requesting money */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /** User who is being asked to pay */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    /** Transaction created when request is fulfilled */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    // ── Audit ────────────────────────────────────────────────

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // ── Constructors ─────────────────────────────────────────

    public MoneyRequest() {}

    public MoneyRequest(Long id, BigDecimal amount, String currency, String message, MoneyRequestStatus status,
                        Instant expiresAt, User requester, User payer, Transaction transaction,
                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.amount = amount;
        this.currency = currency != null ? currency : "USD";
        this.message = message;
        this.status = status != null ? status : MoneyRequestStatus.PENDING;
        this.expiresAt = expiresAt;
        this.requester = requester;
        this.payer = payer;
        this.transaction = transaction;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Getters and Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public MoneyRequestStatus getStatus() { return status; }
    public void setStatus(MoneyRequestStatus status) { this.status = status; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public User getPayer() { return payer; }
    public void setPayer(User payer) { this.payer = payer; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}