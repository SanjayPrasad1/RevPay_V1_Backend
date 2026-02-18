package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.transaction.TopUpRequest;
import com.revpay.dto.transaction.TransactionResponse;
import com.revpay.dto.transaction.TransferRequest;
import com.revpay.entity.Account;
import com.revpay.entity.PaymentMethod;
import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.enums.AccountStatus;
import com.revpay.enums.TransactionStatus;
import com.revpay.enums.TransactionType;
import com.revpay.repository.AccountRepository;
import com.revpay.repository.PaymentMethodRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              UserRepository userRepository,
                              PaymentMethodRepository paymentMethodRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    // ── Transfer ─────────────────────────────────────────────

    @Transactional
    public TransactionResponse transfer(String email, TransferRequest req) {
        User sender = getUser(email);
        Account senderAccount = getPrimaryAccount(sender);

        User receiver = userRepository.findByEmail(req.getReceiverEmail())
                .orElseThrow(() -> RevPayException.notFound("No user found with that email"));

        Account receiverAccount = accountRepository.findByUserId(receiver.getId())
                .stream().findFirst()
                .orElseThrow(() -> RevPayException.notFound("Receiver has no account"));

        if (senderAccount.getUser().getEmail().equals(req.getReceiverEmail())) {
            throw RevPayException.badRequest("Cannot send money to yourself");
        }

        validateAccountActive(senderAccount);
        validateSufficientBalance(senderAccount, req.getAmount());

        senderAccount.setBalance(senderAccount.getBalance().subtract(req.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(req.getAmount()));

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        Transaction tx = buildTransaction(
                senderAccount, receiverAccount,
                req.getAmount(), TransactionType.TRANSFER,
                req.getDescription(), req.getNote(), null
        );

        return toResponse(transactionRepository.save(tx));
    }

    // ── Top Up ───────────────────────────────────────────────

    @Transactional
    public TransactionResponse topUp(String email, TopUpRequest req) {
        User user = getUser(email);
        Account account = getPrimaryAccount(user);

        PaymentMethod pm = paymentMethodRepository
                .findByIdAndUserId(req.getPaymentMethodId(), user.getId())
                .orElseThrow(() -> RevPayException.notFound("Payment method not found"));

        validateAccountActive(account);

        account.setBalance(account.getBalance().add(req.getAmount()));
        accountRepository.save(account);

        Transaction tx = buildTransaction(
                account, account,
                req.getAmount(), TransactionType.DEPOSIT,
                req.getDescription() != null ? req.getDescription() : "Wallet top-up",
                null, pm
        );
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setCompletedAt(Instant.now());

        return toResponse(transactionRepository.save(tx));
    }

    // ── History ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getHistory(String email, Pageable pageable) {
        User user = getUser(email);
        Account account = getPrimaryAccount(user);
        return transactionRepository
                .findAllByAccountId(account.getId(), pageable)
                .map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(String email, Pageable pageable) {
        User user = getUser(email);
        Account account = accountRepository.findByUser(user)
                .stream().findFirst()
                .orElseThrow(() -> RevPayException.notFound("Account not found"));

        return transactionRepository
                .findBySenderAccountOrReceiverAccount(account, account, pageable)
                .map(tx -> toResponse(tx, account));
    }

    // ── Helpers ──────────────────────────────────────────────

    private Transaction buildTransaction(Account sender, Account receiver,
                                         BigDecimal amount, TransactionType type,
                                         String description, String note,
                                         PaymentMethod pm) {
        Transaction tx = new Transaction();
        tx.setReferenceNumber("TXN" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase());
        tx.setSenderAccount(sender);
        tx.setReceiverAccount(receiver);
        tx.setAmount(amount);
        tx.setFee(BigDecimal.ZERO);
        tx.setCurrency("USD");
        tx.setType(type);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setCompletedAt(Instant.now());
        tx.setDescription(description);
        tx.setNote(note);
        tx.setPaymentMethod(pm);
        return tx;
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw RevPayException.badRequest("Account is not active");
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw RevPayException.badRequest("Insufficient balance");
        }
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

//    public TransactionResponse toResponse(Transaction tx) {
//        TransactionResponse r = new TransactionResponse();
//        r.setId(tx.getId());
//        r.setReferenceNumber(tx.getReferenceNumber());
//        r.setType(tx.getType());
//        r.setStatus(tx.getStatus());
//        r.setAmount(tx.getAmount());
//        r.setFee(tx.getFee());
//        r.setCurrency(tx.getCurrency());
//        r.setDescription(tx.getDescription());
//        r.setNote(tx.getNote());
//        r.setSenderAccountId(tx.getSenderAccount().getId());
//        r.setSenderAccountNumber(tx.getSenderAccount().getAccountNumber());
//        r.setReceiverAccountId(tx.getReceiverAccount().getId());
//        r.setReceiverAccountNumber(tx.getReceiverAccount().getAccountNumber());
//        r.setCreatedAt(tx.getCreatedAt());
//        r.setCompletedAt(tx.getCompletedAt());
//        return r;
//    }
//    public TransactionResponse toResponse(Transaction tx) {
//        Account viewer = tx.getSenderAccount() != null
//                ? tx.getSenderAccount()
//                : tx.getReceiverAccount();
//        return toResponse(tx, viewer);
//    }

    public TransactionResponse toResponse(Transaction tx) {
        // For DEPOSIT — viewer is the receiver (money coming in)
        if (tx.getType() == TransactionType.DEPOSIT) {
            Account viewer = tx.getReceiverAccount() != null
                    ? tx.getReceiverAccount()
                    : tx.getSenderAccount();
            return toResponse(tx, viewer);
        }
        // For others — viewer is sender
        Account viewer = tx.getSenderAccount() != null
                ? tx.getSenderAccount()
                : tx.getReceiverAccount();
        return toResponse(tx, viewer);
    }

    /*private TransactionResponse toResponse(Transaction tx, Account viewerAccount) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setReferenceNumber(tx.getReferenceNumber());
        r.setType(tx.getType());
        r.setStatus(tx.getStatus());
        r.setAmount(tx.getAmount());
        r.setFee(tx.getFee());
        r.setCurrency(tx.getCurrency());
        r.setDescription(tx.getDescription());
        r.setNote(tx.getNote());
        r.setCreatedAt(tx.getCreatedAt());
        r.setCompletedAt(tx.getCompletedAt());

        // Handle null sender (loan disbursement = system credit)
        if (tx.getSenderAccount() == null) {
            r.setCredit(true);
            r.setSenderAccountNumber("SYSTEM");
            r.setReceiverAccountNumber(
                    tx.getReceiverAccount() != null
                            ? tx.getReceiverAccount().getAccountNumber() : "");
        } else if (tx.getReceiverAccount() != null
                && tx.getReceiverAccount().getId().equals(viewerAccount.getId())) {
            r.setCredit(true);
            r.setSenderAccountNumber(tx.getSenderAccount().getAccountNumber());
            r.setReceiverAccountNumber(tx.getReceiverAccount().getAccountNumber());
        } else {
            r.setCredit(false);
            r.setSenderAccountNumber(tx.getSenderAccount().getAccountNumber());
            r.setReceiverAccountNumber(
                    tx.getReceiverAccount() != null
                            ? tx.getReceiverAccount().getAccountNumber() : "");
        }

        return r;
    }*/
    private TransactionResponse toResponse(Transaction tx, Account viewerAccount) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setReferenceNumber(tx.getReferenceNumber());
        r.setType(tx.getType());
        r.setStatus(tx.getStatus());
        r.setAmount(tx.getAmount());
        r.setFee(tx.getFee());
        r.setCurrency(tx.getCurrency());
        r.setDescription(tx.getDescription());
        r.setNote(tx.getNote());
        r.setCreatedAt(tx.getCreatedAt());
        r.setCompletedAt(tx.getCompletedAt());

        // ── Credit/Debit logic ──
        if (tx.getType() == TransactionType.DEPOSIT) {
            // Top-up always credit
            r.setCredit(true);
            r.setSenderAccountNumber("EXTERNAL");
            r.setReceiverAccountNumber(
                    tx.getReceiverAccount() != null
                            ? tx.getReceiverAccount().getAccountNumber() : "");

        } else if (tx.getSenderAccount() == null) {
            // System credit (loan disbursement)
            r.setCredit(true);
            r.setSenderAccountNumber("SYSTEM");
            r.setReceiverAccountNumber(
                    tx.getReceiverAccount() != null
                            ? tx.getReceiverAccount().getAccountNumber() : "");

        } else if (tx.getReceiverAccount() != null
                && tx.getReceiverAccount().getId().equals(viewerAccount.getId())
                && !tx.getSenderAccount().getId().equals(viewerAccount.getId())) {
            // Viewer is receiver and NOT sender — credit
            r.setCredit(true);
            r.setSenderAccountNumber(tx.getSenderAccount().getAccountNumber());
            r.setReceiverAccountNumber(tx.getReceiverAccount().getAccountNumber());

        } else {
            // Viewer is sender — debit
            r.setCredit(false);
            r.setSenderAccountNumber(tx.getSenderAccount().getAccountNumber());
            r.setReceiverAccountNumber(
                    tx.getReceiverAccount() != null
                            ? tx.getReceiverAccount().getAccountNumber() : "SYSTEM");
        }

        return r;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsForExport(
            String email, String fromDate, String toDate) {

        User user = getUser(email);
        Account account = accountRepository.findByUser(user)
                .stream().findFirst()
                .orElseThrow(() -> RevPayException.notFound("Account not found"));

        List<Transaction> all = transactionRepository
                .findBySenderAccountOrReceiverAccountOrderByCreatedAtDesc(
                        account, account);

        return all.stream()
                .filter(tx -> {
                    if (fromDate == null && toDate == null) return true;
                    Instant from = fromDate != null
                            ? LocalDate.parse(fromDate).atStartOfDay(ZoneOffset.UTC).toInstant()
                            : Instant.MIN;
                    Instant to = toDate != null
                            ? LocalDate.parse(toDate).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                            : Instant.MAX;
                    Instant txTime = tx.getCreatedAt() != null ? tx.getCreatedAt() : Instant.MIN;
                    return txTime.isAfter(from) && txTime.isBefore(to);
                })
                .map(tx -> toResponse(tx, account))
                .collect(Collectors.toList());
    }

}