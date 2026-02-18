package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.emi.EMIResponse;
import com.revpay.entity.*;
import com.revpay.enums.EMIStatus;
import com.revpay.enums.LoanStatus;
import com.revpay.enums.TransactionStatus;
import com.revpay.enums.TransactionType;
import com.revpay.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EMIService {

    private final EMIRepository emiRepository;
    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // Fine rate — 2% of EMI amount per overdue EMI
    private static final BigDecimal FINE_RATE = new BigDecimal("0.02");

    public EMIService(EMIRepository emiRepository,
                      LoanRepository loanRepository,
                      AccountRepository accountRepository,
                      TransactionRepository transactionRepository,
                      UserRepository userRepository) {
        this.emiRepository = emiRepository;
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    // ── Get EMI schedule for a loan ──────────────────────────

    public List<EMIResponse> getEmiSchedule(String email, Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> RevPayException.notFound("Loan not found"));

        if (!loan.getBorrower().getEmail().equals(email)) {
            throw RevPayException.forbidden("Not authorized");
        }

        return emiRepository.findByLoanIdOrderByInstalmentNumberAsc(loanId)
                .stream().map(this::toResponse).toList();
    }

    // ── Manual EMI payment ───────────────────────────────────

    @Transactional
    public EMIResponse payEmi(String email, Long emiId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> RevPayException.notFound("User not found"));

        EMI emi = emiRepository.findById(emiId)
                .orElseThrow(() -> RevPayException.notFound("EMI not found"));

        if (!emi.getLoan().getBorrower().getEmail().equals(email)) {
            throw RevPayException.forbidden("Not authorized");
        }

        if (emi.getStatus() == EMIStatus.PAID) {
            throw RevPayException.badRequest("EMI already paid");
        }

        Account borrowerAccount = accountRepository.findByUser(user)
                .stream().findFirst()
                .orElseThrow(() -> RevPayException.notFound("Account not found"));

        // Total amount = EMI + fine if overdue
        BigDecimal totalDue = emi.getAmount();
        if (emi.getFineAmount() != null) {
            totalDue = totalDue.add(emi.getFineAmount());
        }

        if (borrowerAccount.getBalance().compareTo(totalDue) < 0) {
            throw RevPayException.badRequest(
                    "Insufficient balance. Required: $" + totalDue);
        }

        System.out.println("Before deduction - Balance: " + borrowerAccount.getBalance());

        // Deduct from account
        borrowerAccount.setBalance(
                borrowerAccount.getBalance().subtract(totalDue));

        System.out.println("After deduction - Balance: " + borrowerAccount.getBalance());
        accountRepository.save(borrowerAccount);

        System.out.println("Saved account: " + borrowerAccount.getId());

        // Update loan repaid amount
        Loan loan = emi.getLoan();
        loan.setAmountRepaid(
                loan.getAmountRepaid().add(emi.getAmount()));

        // Check if all EMIs paid — close loan
        long remainingEMIs = emiRepository.findPendingByLoanId(loan.getId())
                .stream().filter(e -> !e.getId().equals(emiId)).count();
        if (remainingEMIs == 0) {
            loan.setStatus(LoanStatus.CLOSED);
        }
        loanRepository.save(loan);

        // Record transaction
        Transaction tx = new Transaction();
        tx.setReferenceNumber("EMI" + UUID.randomUUID()
                .toString().replace("-","").substring(0,12).toUpperCase());
        tx.setSenderAccount(borrowerAccount);
        tx.setReceiverAccount(null);
        tx.setAmount(totalDue);
        tx.setFee(BigDecimal.ZERO);
        tx.setCurrency("USD");
        tx.setType(TransactionType.EMI_PAYMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription("EMI payment #" + emi.getInstalmentNumber()
                + " — " + loan.getLoanNumber()
                + (emi.getFineAmount() != null
                ? " (incl. fine: $" + emi.getFineAmount() + ")" : ""));
        tx.setCompletedAt(Instant.now());
        transactionRepository.save(tx);

        // Mark EMI paid
        emi.setStatus(EMIStatus.PAID);
        emi.setPaidDate(LocalDate.now());
        emi.setTransaction(tx);
        emiRepository.save(emi);

        return toResponse(emi);
    }

    // ── Toggle auto-debit ────────────────────────────────────

    @Transactional
    public void toggleAutoDebit(String email, Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> RevPayException.notFound("Loan not found"));

        if (!loan.getBorrower().getEmail().equals(email)) {
            throw RevPayException.forbidden("Not authorized");
        }

        loan.setAutoDebit(!loan.isAutoDebit());
        loanRepository.save(loan);
    }

    // ── Scheduled: impose fine on overdue EMIs ───────────────

    @Scheduled(cron = "0 0 9 * * *") // Every day at 9 AM
    @Transactional
    public void imposeOverdueFines() {
        List<EMI> overdueEmis = emiRepository
                .findOverdueWithoutFine(LocalDate.now());

        for (EMI emi : overdueEmis) {
            BigDecimal fine = emi.getAmount()
                    .multiply(FINE_RATE)
                    .setScale(2, RoundingMode.HALF_UP);
            emi.setFineAmount(fine);
            emi.setStatus(EMIStatus.OVERDUE);
            emiRepository.save(emi);
        }
    }

    // ── Scheduled: auto-debit due EMIs ──────────────────────

    @Scheduled(cron = "0 30 9 * * *") // Every day at 9:30 AM
    @Transactional
    public void processAutoDebits() {
        List<EMI> dueEmis = emiRepository
                .findDueForAutoDebit(LocalDate.now());

        for (EMI emi : dueEmis) {
            try {
                payEmi(emi.getLoan().getBorrower().getEmail(), emi.getId());
            } catch (Exception e) {
                // Log but continue — insufficient balance etc
                System.err.println("Auto-debit failed for EMI "
                        + emi.getId() + ": " + e.getMessage());
            }
        }
    }

    // ── Helper ───────────────────────────────────────────────

    public EMIResponse toResponse(EMI emi) {
        EMIResponse r = new EMIResponse();
        r.setId(emi.getId());
        r.setInstalmentNumber(emi.getInstalmentNumber());
        r.setAmount(emi.getAmount());
        r.setPrincipalComponent(emi.getPrincipalComponent());
        r.setInterestComponent(emi.getInterestComponent());
        r.setFineAmount(emi.getFineAmount());
        r.setDueDate(emi.getDueDate());
        r.setPaidDate(emi.getPaidDate());
        r.setStatus(EMIStatus.valueOf(emi.getStatus().name()));
        r.setTotalDue(emi.getFineAmount() != null
                ? emi.getAmount().add(emi.getFineAmount())
                : emi.getAmount());
        return r;
    }
}