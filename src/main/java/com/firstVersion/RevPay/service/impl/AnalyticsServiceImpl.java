package com.firstVersion.RevPay.service.impl;

import com.firstVersion.RevPay.dto.DashboardDTO;
import com.firstVersion.RevPay.entity.Transaction;
import com.firstVersion.RevPay.entity.User;
import com.firstVersion.RevPay.repository.MoneyRequestRepository;
import com.firstVersion.RevPay.repository.TransactionRepository;
import com.firstVersion.RevPay.repository.UserRepository;
import com.firstVersion.RevPay.service.AnalyticsService;
import com.firstVersion.RevPay.entity.MoneyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MoneyRequestRepository moneyRequestRepository;

    @Override
    public DashboardDTO getUserDashboard(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setCurrentBalance(user.getWalletBalance());

        // Calculate Monthly Spending (where user is the sender)
        List<Transaction> outgoing = transactionRepository.findAll().stream()
                .filter(t -> t.getSender().getEmail().equals(email) && t.getTimestamp().isAfter(startOfMonth))
                .toList();

        BigDecimal spent = outgoing.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate Monthly Income (where user is the receiver)
        List<Transaction> incoming = transactionRepository.findAll().stream()
                .filter(t -> t.getReceiver().getEmail().equals(email) && t.getTimestamp().isAfter(startOfMonth))
                .toList();

        BigDecimal received = incoming.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dashboard.setTotalSpentMonth(spent);
        dashboard.setTotalReceivedMonth(received);

        // Count Pending Requests
        long pending = moneyRequestRepository.findByTargetUserAndStatus(user, MoneyRequest.RequestStatus.PENDING).size();
        dashboard.setPendingRequestsCount(pending);

        return dashboard;
    }
}