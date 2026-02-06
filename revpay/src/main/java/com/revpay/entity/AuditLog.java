package com.revpay.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_entity", columnList = "entityName, entityId"),
        @Index(name = "idx_audit_created", columnList = "createdAt")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Which entity was affected e.g. "Transaction", "Account", "Loan" */
    @Column(nullable = false)
    private String entityName;

    /** ID of the affected entity */
    @Column(nullable = false)
    private Long entityId;

    /** Action performed e.g. "CREATE", "UPDATE", "DELETE", "LOGIN", "TRANSFER" */
    @Column(nullable = false)
    private String action;

    /** Snapshot of data before the change (JSON string) */
    @Column(columnDefinition = "TEXT")
    private String oldValue;

    /** Snapshot of data after the change (JSON string) */
    @Column(columnDefinition = "TEXT")
    private String newValue;

    /** IP address of the request origin */
    private String ipAddress;

    /** User-Agent / client info */
    private String userAgent;

    /** Additional context or description */
    private String description;

    // ── Relationships ────────────────────────────────────────

    /** User who performed the action — nullable for system-triggered actions */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User performedBy;

    // ── Audit ────────────────────────────────────────────────

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    // ── Constructors ─────────────────────────────────────────

    public AuditLog() {}

    // ── Getters & Setters ────────────────────────────────────

    public Long getId() { return id; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getPerformedBy() { return performedBy; }
    public void setPerformedBy(User performedBy) { this.performedBy = performedBy; }

    public Instant getCreatedAt() { return createdAt; }
}