package com.firstVersion.RevPay.service.impl;

import com.firstVersion.RevPay.dto.MoneyRequestDTO;
import com.firstVersion.RevPay.dto.TransactionRequest;
import com.firstVersion.RevPay.entity.MoneyRequest;
import com.firstVersion.RevPay.entity.User;
import com.firstVersion.RevPay.entity.Notification;
import com.firstVersion.RevPay.repository.MoneyRequestRepository;
import com.firstVersion.RevPay.repository.UserRepository;
import com.firstVersion.RevPay.service.MoneyRequestService;
import com.firstVersion.RevPay.service.TransactionService;
import com.firstVersion.RevPay.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MoneyRequestServiceImpl implements MoneyRequestService {

    @Autowired private MoneyRequestRepository requestRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TransactionService transactionService;
    @Autowired private NotificationService notificationService;

    @Override
    public MoneyRequest createRequest(String requesterEmail, MoneyRequestDTO dto) {
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow();
        User target = userRepository.findByEmail(dto.getTargetUserEmail())
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        MoneyRequest request = new MoneyRequest();
        request.setRequester(requester);
        request.setTargetUser(target);
        request.setAmount(dto.getAmount());
        request.setPurpose(dto.getPurpose());
        request.setStatus(MoneyRequest.RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        notificationService.sendNotification(target,
                requester.getFullName() + " requested $" + dto.getAmount() + " for: " + dto.getPurpose(),
                Notification.NotificationType.TRANSACTION);

        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public void acceptRequest(Long requestId) {
        MoneyRequest request = requestRepository.findById(requestId).orElseThrow();

        // Use TransactionService to move the money
        TransactionRequest txRequest = new TransactionRequest();
        txRequest.setReceiverEmail(request.getRequester().getEmail());
        txRequest.setAmount(request.getAmount());
        txRequest.setNote("Accepted request: " + request.getPurpose());

        // The person accepting is the 'Sender'
        transactionService.sendMoney(request.getTargetUser().getEmail(), txRequest);

        request.setStatus(MoneyRequest.RequestStatus.ACCEPTED);
        requestRepository.save(request);
    }

    @Override
    public void declineRequest(Long requestId) {
        MoneyRequest request = requestRepository.findById(requestId).orElseThrow();
        request.setStatus(MoneyRequest.RequestStatus.DECLINED);
        requestRepository.save(request);
    }

    @Override
    public List<MoneyRequest> getMyIncomingRequests(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return requestRepository.findByTargetUserAndStatus(user, MoneyRequest.RequestStatus.PENDING);
    }
}