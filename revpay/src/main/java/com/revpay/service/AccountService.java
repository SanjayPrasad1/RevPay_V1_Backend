package com.revpay.service;

import com.revpay.common.RevPayException;
import com.revpay.dto.account.AccountResponse;
import com.revpay.entity.Account;
import com.revpay.entity.User;
import com.revpay.repository.AccountRepository;
import com.revpay.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(String email) {
        User user = getUser(email);
        return accountRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId, String email) {
        User user = getUser(email);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> RevPayException.notFound("Account not found"));
        if (!account.getUser().getId().equals(user.getId())) {
            throw RevPayException.forbidden("Access denied");
        }
        return toResponse(account);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> RevPayException.notFound("User not found"));
    }

    public AccountResponse toResponse(Account account) {
        AccountResponse r = new AccountResponse();
        r.setId(account.getId());
        r.setAccountNumber(account.getAccountNumber());
        r.setAccountType(account.getAccountType());
        r.setStatus(account.getStatus());
        r.setBalance(account.getBalance());
        r.setCurrency(account.getCurrency());
        r.setCreatedAt(account.getCreatedAt());
        return r;
    }
}