package com.revpay.entity;

import com.revpay.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principalAmount;

    /** Annual interest rate as a percentage, e.g. 12.50 */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    /** Loan term in months */
    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyEmiAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalRepayableAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal amountRepaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.APPLIED;

    private String purpose;
    private LocalDate disbursementDate;
    private LocalDate closureDate;

    @Column(nullable = false)
    private boolean autoDebit = false;

    public boolean isAutoDebit() { return autoDebit; }
    public void setAutoDebit(boolean autoDebit) { this.autoDebit = autoDebit; }

    // ── Relationships ────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    /** Account to which loan is disbursed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_account_id")
    private Account creditAccount;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EMI> emis = new ArrayList<>();

    // ── Audit ────────────────────────────────────────────────

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}