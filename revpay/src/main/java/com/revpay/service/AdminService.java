package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.admin.AdminUserResponse;
import com.revpay.dto.loan.LoanResponse;
import com.revpay.entity.*;
import com.revpay.enums.LoanStatus;
import com.revpay.enums.Role;
import com.revpay.enums.TransactionStatus;
import com.revpay.enums.TransactionType;
import com.revpay.repository.*;
import com.revpay.enums.EMIStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final EMIRepository emiRepository;

    public AdminService(UserRepository userRepository,
                        AccountRepository accountRepository, LoanRepository loanRepository, TransactionRepository transactionRepository, EMIRepository emiRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.transactionRepository = transactionRepository;
        this.emiRepository = emiRepository;
    }

    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public AdminUserResponse getUserById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> RevPayException.notFound("User not found")));
    }

    public AdminUserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> RevPayException.notFound("User not found"));

        if(user.getRole() == Role.ADMIN){
            throw  RevPayException.badRequest("Cannot disable admin accounts");
        }

        user.setEnabled(!user.isEnabled());
        return toResponse(userRepository.save(user));
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getTotalByRole(String role) {
        return userRepository.countByRole(
                com.revpay.enums.Role.valueOf(role));
    }

    private AdminUserResponse toResponse(User user) {
        AdminUserResponse r = new AdminUserResponse();
        r.setId(user.getId());
        r.setFullName(user.getFullName());
        r.setEmail(user.getEmail());
        r.setPhone(user.getPhone());
        r.setRole(user.getRole());
        r.setEnabled(user.isEnabled());
        r.setCreatedAt(user.getCreatedAt());

        accountRepository.findByUserId(user.getId())
                .stream().findFirst().ifPresent(acc -> {
                    r.setBalance(acc.getBalance());
                    r.setAccountNumber(acc.getAccountNumber());
                });
        return r;
    }
    public Page<LoanResponse> getAllLoans(String status, Pageable pageable) {
        if (status != null && !status.equals("ALL")) {
            return loanRepository.findByStatus(
                            LoanStatus.valueOf(status), pageable)
                    .map(this::toLoanResponse);
        }
        return loanRepository.findAll(pageable).map(this::toLoanResponse);
    }

    @Transactional
    public LoanResponse approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> RevPayException.notFound("Loan not found"));

        if (loan.getStatus() != LoanStatus.APPLIED) {
            throw RevPayException.badRequest("Only APPLIED loans can be approved");
        }

        // Disburse loan amount to borrower account
        Account borrowerAccount = accountRepository
                .findByUserId(loan.getBorrower().getId())
                .stream().findFirst()
                .orElseThrow(() -> RevPayException.notFound("Account not found"));

        borrowerAccount.setBalance(
                borrowerAccount.getBalance().add(loan.getPrincipalAmount()));
        accountRepository.save(borrowerAccount);

        // Generate EMI schedule
        generateEmiSchedule(loan);

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());
        loanRepository.save(loan);

        // Create disbursement transaction
        Transaction tx = new Transaction();
        tx.setReferenceNumber("LN" + UUID.randomUUID()
                .toString().replace("-","").substring(0,12).toUpperCase());
        tx.setReceiverAccount(borrowerAccount);
        tx.setSenderAccount(null);
        tx.setAmount(loan.getPrincipalAmount());
        tx.setFee(BigDecimal.ZERO);
        tx.setCurrency("USD");
        tx.setType(TransactionType.LOAN_DISBURSEMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription("Loan disbursement: " + loan.getLoanNumber()
                + " | Principal: $" + loan.getPrincipalAmount()
                + " | EMI: $" + loan.getMonthlyEmiAmount()
                + "/mo for " + loan.getTenureMonths() + " months");
        tx.setCompletedAt(Instant.now());
        transactionRepository.save(tx);

        return toLoanResponse(loan);
    }

    @Transactional
    public LoanResponse rejectLoan(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> RevPayException.notFound("Loan not found"));

        if (loan.getStatus() != LoanStatus.APPLIED) {
            throw RevPayException.badRequest("Only APPLIED loans can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setPurpose(loan.getPurpose() + " | Rejected: " + reason);
        return toLoanResponse(loanRepository.save(loan));
    }

    private void generateEmiSchedule(Loan loan) {
        emiRepository.deleteByLoanId(loan.getId());

        BigDecimal principal = loan.getPrincipalAmount();
        BigDecimal annualRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100));
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        int months = loan.getTenureMonths();

        LocalDate dueDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= months; i++) {
            // Calculate interest and principal components
            BigDecimal interestComponent = principal.multiply(monthlyRate)
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal principalComponent = loan.getMonthlyEmiAmount()
                    .subtract(interestComponent)
                    .setScale(4, RoundingMode.HALF_UP);

            EMI emi = new EMI();
            emi.setLoan(loan);
            emi.setInstalmentNumber(i);
            emi.setAmount(loan.getMonthlyEmiAmount());
            emi.setPrincipalComponent(principalComponent);
            emi.setInterestComponent(interestComponent);
            emi.setDueDate(dueDate);
            emi.setStatus(EMIStatus.PENDING);
            emiRepository.save(emi);

            principal = principal.subtract(principalComponent);
            dueDate = dueDate.plusMonths(1);
        }
    }

    private LoanResponse toLoanResponse(Loan loan) {
        LoanResponse r = new LoanResponse();
        r.setId(loan.getId());
        r.setLoanNumber(loan.getLoanNumber());
        r.setBorrowerName(loan.getBorrower().getFullName());
        r.setBorrowerEmail(loan.getBorrower().getEmail());
        r.setStatus(loan.getStatus());
        r.setPrincipalAmount(loan.getPrincipalAmount());
        r.setInterestRate(loan.getInterestRate());
        r.setTenureMonths(loan.getTenureMonths());
        r.setMonthlyEmiAmount(loan.getMonthlyEmiAmount());
        r.setTotalRepayableAmount(loan.getTotalRepayableAmount());
        r.setAmountRepaid(loan.getAmountRepaid());
        r.setPurpose(loan.getPurpose());
        r.setDisbursementDate(loan.getDisbursementDate());
        r.setCreatedAt(loan.getCreatedAt());
        return r;
    }

    public long getPendingLoansCount() {
        return loanRepository.countByStatus(LoanStatus.APPLIED);
    }

}