package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.emi.EMIResponse;
import com.revpay.dto.loan.ApplyLoanRequest;
import com.revpay.dto.loan.LoanResponse;
import com.revpay.entity.*;
import com.revpay.enums.EMIStatus;
import com.revpay.enums.LoanStatus;
import com.revpay.enums.TransactionStatus;
import com.revpay.enums.TransactionType;
import com.revpay.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

    private static final BigDecimal ANNUAL_RATE = new BigDecimal("12.00");

    private final LoanRepository loanRepository;
    private final EMIRepository emiRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public LoanService(LoanRepository loanRepository,
                       EMIRepository emiRepository,
                       UserRepository userRepository,
                       AccountRepository accountRepository,
                       TransactionRepository transactionRepository) {
        this.loanRepository = loanRepository;
        this.emiRepository = emiRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // ── Apply ────────────────────────────────────────────────

    @Transactional
    public LoanResponse applyForLoan(String email, ApplyLoanRequest req) {
        User borrower = getUser(email);

        BigDecimal monthlyRate = ANNUAL_RATE
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        int n = req.getTenureMonths();

        BigDecimal factor = monthlyRate.add(BigDecimal.ONE).pow(n);
        BigDecimal emi = req.getPrincipalAmount()
                .multiply(monthlyRate).multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

        BigDecimal total = emi.multiply(BigDecimal.valueOf(n));

        Loan loan = new Loan();
        loan.setBorrower(borrower);
        loan.setLoanNumber("LN-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase());
        loan.setStatus(LoanStatus.APPLIED);
        loan.setPrincipalAmount(req.getPrincipalAmount());
        loan.setInterestRate(ANNUAL_RATE);
        loan.setTenureMonths(n);
        loan.setMonthlyEmiAmount(emi);
        loan.setTotalRepayableAmount(total);
        loan.setAmountRepaid(BigDecimal.ZERO);
        loan.setPurpose(req.getPurpose());

        return toResponse(loanRepository.save(loan), List.of());
    }

    // ── Repay EMI ────────────────────────────────────────────

    @Transactional
    public LoanResponse repayEmi(String email, Long loanId, Long emiId) {
        User borrower = getUser(email);
        Loan loan = getLoan(loanId);

        if (!loan.getBorrower().getId().equals(borrower.getId())) {
            throw RevPayException.forbidden("Not authorized");
        }
        if (loan.getStatus() != LoanStatus.ACTIVE &&
                loan.getStatus() != LoanStatus.DISBURSED) {
            throw RevPayException.badRequest("Loan is not active");
        }

        EMI emi = emiRepository.findById(emiId)
                .orElseThrow(() -> RevPayException.notFound("EMI not found"));

        if (!emi.getLoan().getId().equals(loanId)) {
            throw RevPayException.badRequest("EMI does not belong to this loan");
        }
        if (emi.getStatus() == EMIStatus.PAID) {
            throw RevPayException.badRequest("EMI already paid");
        }

        Account account = getPrimaryAccount(borrower);
        if (account.getBalance().compareTo(emi.getAmount()) < 0) {
            throw RevPayException.badRequest("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(emi.getAmount()));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setReferenceNumber("TXN" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase());
        tx.setSenderAccount(account);
        tx.setReceiverAccount(account);
        tx.setAmount(emi.getAmount());
        tx.setFee(BigDecimal.ZERO);
        tx.setCurrency("USD");
        tx.setType(TransactionType.EMI_PAYMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setCompletedAt(Instant.now());
        tx.setDescription("EMI payment for loan " + loan.getLoanNumber());
        transactionRepository.save(tx);

        emi.setStatus(EMIStatus.PAID);
        emi.setPaidDate(LocalDate.now());
        emi.setTransaction(tx);
        emiRepository.save(emi);

        loan.setAmountRepaid(loan.getAmountRepaid().add(emi.getAmount()));

        long unpaid = emiRepository.countByLoanIdAndStatus(loanId, EMIStatus.SCHEDULED);
        if (unpaid == 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosureDate(LocalDate.now());
        }
        loanRepository.save(loan);

        List<EMI> emis = emiRepository.findByLoanId(loanId);
        return toResponse(loan, emis);
    }

    // ── List ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LoanResponse> getMyLoans(String email, Pageable pageable) {
        User user = getUser(email);
        return loanRepository.findByBorrowerId(user.getId(), pageable)
                .map(loan -> {
                    List<EMI> emis = emiRepository.findByLoanId(loan.getId());
                    return toResponse(loan, emis);
                });
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoanById(String email, Long id) {
        getUser(email);
        Loan loan = getLoan(id);
        List<EMI> emis = emiRepository.findByLoanId(id);
        return toResponse(loan, emis);
    }

    // ── Helpers ──────────────────────────────────────────────

    private Loan getLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> RevPayException.notFound("Loan not found"));
    }

    private Account getPrimaryAccount(User user) {
        return accountRepository.findByUserId(user.getId())
                .stream().findFirst()
                .orElseThrow(() -> RevPayException.notFound("No account found"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> RevPayException.notFound("User not found"));
    }

    public LoanResponse toResponse(Loan loan, List<EMI> emis) {
        LoanResponse r = new LoanResponse();
        r.setId(loan.getId());
        r.setLoanNumber(loan.getLoanNumber());
        r.setStatus(loan.getStatus());
        r.setPrincipalAmount(loan.getPrincipalAmount());
        r.setInterestRate(loan.getInterestRate());
        r.setTenureMonths(loan.getTenureMonths());
        r.setMonthlyEmiAmount(loan.getMonthlyEmiAmount());
        r.setTotalRepayableAmount(loan.getTotalRepayableAmount());
        r.setAmountRepaid(loan.getAmountRepaid());
        r.setPurpose(loan.getPurpose());
        r.setDisbursementDate(loan.getDisbursementDate());
        r.setClosureDate(loan.getClosureDate());
        r.setCreatedAt(loan.getCreatedAt());
        r.setAutoDebit(loan.isAutoDebit());
        r.setEmis(emis.stream().map(emi -> {
            EMIResponse er = new EMIResponse();
            er.setId(emi.getId());
            er.setInstalmentNumber(emi.getInstalmentNumber());
            er.setAmount(emi.getAmount());
            er.setPrincipalComponent(emi.getPrincipalComponent());
            er.setInterestComponent(emi.getInterestComponent());
            er.setDueDate(emi.getDueDate());
            er.setPaidDate(emi.getPaidDate());
            er.setStatus(emi.getStatus());
            return er;
        }).toList());
        return r;
    }
}