package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.moneyrequest.MoneyRequestDto;
import com.revpay.dto.moneyrequest.MoneyRequestResponse;
import com.revpay.entity.Account;
import com.revpay.entity.MoneyRequest;
import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.enums.MoneyRequestStatus;
import com.revpay.enums.TransactionStatus;
import com.revpay.enums.TransactionType;
import com.revpay.repository.AccountRepository;
import com.revpay.repository.MoneyRequestRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class MoneyRequestService {

    private final MoneyRequestRepository moneyRequestRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public MoneyRequestService(MoneyRequestRepository moneyRequestRepository,
                               UserRepository userRepository,
                               AccountRepository accountRepository,
                               TransactionRepository transactionRepository) {
        this.moneyRequestRepository = moneyRequestRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // ── Send Request ─────────────────────────────────────────

    @Transactional
    public MoneyRequestResponse sendRequest(String email, MoneyRequestDto dto) {
        User requester = getUser(email);
        User payer = userRepository.findByEmail(dto.getPayerIdentifier())
                .orElseThrow(() -> RevPayException.notFound(
                        "No user found with identifier: " + dto.getPayerIdentifier()));

        if (requester.getId().equals(payer.getId())) {
            throw RevPayException.badRequest("Cannot request money from yourself");
        }

        MoneyRequest request = new MoneyRequest();
        request.setRequester(requester);
        request.setPayer(payer);
        request.setAmount(dto.getAmount());
        request.setCurrency("USD");
        request.setMessage(dto.getMessage());
        request.setStatus(MoneyRequestStatus.PENDING);
        request.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));

        return toResponse(moneyRequestRepository.save(request));
    }

    // ── Accept Request ───────────────────────────────────────

    @Transactional
    public MoneyRequestResponse acceptRequest(String email, Long requestId) {
        User payer = getUser(email);
        MoneyRequest request = getRequest(requestId);

        if (!request.getPayer().getId().equals(payer.getId())) {
            throw RevPayException.forbidden("Not authorized to accept this request");
        }
        if (request.getStatus() != MoneyRequestStatus.PENDING) {
            throw RevPayException.badRequest("Request is no longer pending");
        }

        Account payerAccount = getPrimaryAccount(payer);
        Account requesterAccount = getPrimaryAccount(request.getRequester());

        if (payerAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw RevPayException.badRequest("Insufficient balance");
        }

        payerAccount.setBalance(payerAccount.getBalance().subtract(request.getAmount()));
        requesterAccount.setBalance(requesterAccount.getBalance().add(request.getAmount()));
        accountRepository.save(payerAccount);
        accountRepository.save(requesterAccount);

        Transaction tx = new Transaction();
        tx.setReferenceNumber("TXN" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase());
        tx.setSenderAccount(payerAccount);
        tx.setReceiverAccount(requesterAccount);
        tx.setAmount(request.getAmount());
        tx.setFee(BigDecimal.ZERO);
        tx.setCurrency("USD");
        tx.setType(TransactionType.MONEY_REQUEST_FULFILLMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setCompletedAt(Instant.now());
        tx.setDescription("Money request fulfillment");
        transactionRepository.save(tx);

        request.setStatus(MoneyRequestStatus.ACCEPTED);
        request.setTransaction(tx);
        return toResponse(moneyRequestRepository.save(request));
    }

    // ── Reject Request ───────────────────────────────────────

    @Transactional
    public MoneyRequestResponse rejectRequest(String email, Long requestId) {
        User payer = getUser(email);
        MoneyRequest request = getRequest(requestId);

        if (!request.getPayer().getId().equals(payer.getId())) {
            throw RevPayException.forbidden("Not authorized to reject this request");
        }
        if (request.getStatus() != MoneyRequestStatus.PENDING) {
            throw RevPayException.badRequest("Request is no longer pending");
        }

        request.setStatus(MoneyRequestStatus.REJECTED);
        return toResponse(moneyRequestRepository.save(request));
    }

    // ── List ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<MoneyRequestResponse> getSentRequests(String email, Pageable pageable) {
        User user = getUser(email);
        return moneyRequestRepository.findByRequesterId(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MoneyRequestResponse> getReceivedRequests(String email, Pageable pageable) {
        User user = getUser(email);
        return moneyRequestRepository.findByPayerId(user.getId(), pageable)
                .map(this::toResponse);
    }

    // ── Helpers ──────────────────────────────────────────────

    private MoneyRequest getRequest(Long id) {
        return moneyRequestRepository.findById(id)
                .orElseThrow(() -> RevPayException.notFound("Money request not found"));
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

    public MoneyRequestResponse toResponse(MoneyRequest r) {
        MoneyRequestResponse res = new MoneyRequestResponse();
        res.setId(r.getId());
        res.setAmount(r.getAmount());
        res.setCurrency(r.getCurrency());
        res.setMessage(r.getMessage());
        res.setStatus(r.getStatus());
        res.setRequesterId(r.getRequester().getId());
        res.setRequesterName(r.getRequester().getFullName());
        res.setRequesterEmail(r.getRequester().getEmail());
        res.setPayerId(r.getPayer().getId());
        res.setPayerName(r.getPayer().getFullName());
        res.setPayerEmail(r.getPayer().getEmail());
        res.setCreatedAt(r.getCreatedAt());
        res.setExpiresAt(r.getExpiresAt());
        return res;
    }
}