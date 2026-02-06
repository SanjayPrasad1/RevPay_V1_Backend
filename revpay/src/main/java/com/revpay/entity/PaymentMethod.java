package com.revpay.entity;

import com.revpay.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.YearMonth;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType type;

    /** Last 4 digits / masked identifier */
    @Column(nullable = false)
    private String maskedIdentifier;

    /** Bank name, card network, UPI handle, etc. */
    private String provider;

    /** Expiry – only relevant for cards */
    private String expiryMonth;
    private String expiryYear;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    // ── Relationships ────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Audit ────────────────────────────────────────────────

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}