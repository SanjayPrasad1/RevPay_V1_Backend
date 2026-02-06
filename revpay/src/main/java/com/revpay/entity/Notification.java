package com.revpay.entity;

import com.revpay.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_user_read", columnList = "user_id, isRead")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    /** Optional deep-link reference, e.g. "/transactions/42" */
    private String actionUrl;

    /** Optional ID of the related entity (transactionId, invoiceId, etc.) */
    private Long referenceId;

    // ── Relationships ────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Audit ────────────────────────────────────────────────

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    private Instant readAt;
}