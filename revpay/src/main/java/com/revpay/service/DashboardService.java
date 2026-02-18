package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.dashboard.DashboardResponse;
import com.revpay.dto.transaction.TransactionResponse;
import com.revpay.entity.Account;
import com.revpay.entity.User;
import com.revpay.enums.MoneyRequestStatus;
import com.revpay.repository.AccountRepository;
import com.revpay.repository.MoneyRequestRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MoneyRequestRepository moneyRequestRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public DashboardService(UserRepository userRepository,
                            AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            MoneyRequestRepository moneyRequestRepository,
                            AccountService accountService,
                            TransactionService transactionService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.moneyRequestRepository = moneyRequestRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> RevPayException.notFound("User not found"));

        Account primary = accountRepository.findByUserId(user.getId())
                .stream().findFirst()
                .orElseThrow(() -> RevPayException.notFound("No account found"));

        List<TransactionResponse> recent = transactionRepository
                .findAllByAccountId(primary.getId(), PageRequest.of(0, 5))
                .map(transactionService::toResponse)
                .toList();

        long pendingRequests = moneyRequestRepository
                .countByPayerIdAndStatus(user.getId(), MoneyRequestStatus.PENDING);

        DashboardResponse res = new DashboardResponse();
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole().name());
        res.setPrimaryAccount(accountService.toResponse(primary));
        res.setTotalBalance(primary.getBalance());
        res.setTotalTransactions(transactionRepository.countBySenderAccountId(primary.getId()));
        res.setPendingMoneyRequests(pendingRequests);
        res.setRecentTransactions(recent);
        return res;
    }
}