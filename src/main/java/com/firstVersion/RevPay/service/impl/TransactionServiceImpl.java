package com.firstVersion.RevPay.service.impl;

import com.firstVersion.RevPay.dto.TransactionRequest;
import com.firstVersion.RevPay.dto.TransactionResponse;
import com.firstVersion.RevPay.entity.Notification;
import com.firstVersion.RevPay.entity.Transaction;
import com.firstVersion.RevPay.entity.User;
import com.firstVersion.RevPay.repository.TransactionRepository;
import com.firstVersion.RevPay.repository.UserRepository;
import com.firstVersion.RevPay.service.NotificationService;
import com.firstVersion.RevPay.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public TransactionResponse sendMoney(String senderEmail, TransactionRequest request) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getWalletBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Logic: Transfer Money
        sender.setWalletBalance(sender.getWalletBalance().subtract(request.getAmount()));
        receiver.setWalletBalance(receiver.getWalletBalance().add(request.getAmount()));

        userRepository.save(sender);
        userRepository.save(receiver);

        // Record Transaction
        Transaction tx = new Transaction();
        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(request.getAmount());
        tx.setNote(request.getNote());
        tx.setTimestamp(LocalDateTime.now());

        notificationService.sendNotification(
                receiver,
                "You received $" + request.getAmount() + " from " + sender.getFullName(),
                Notification.NotificationType.TRANSACTION
        );

        Transaction savedTx = transactionRepository.save(tx);

        return mapToResponse(savedTx);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        // Custom logic to find transactions where user is sender OR receiver
        return transactionRepository.findAll().stream() // Simplified for now
                .filter(t -> t.getSender().getEmail().equals(email) || t.getReceiver().getEmail().equals(email))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Inside TransactionServiceImpl.java

    private TransactionResponse mapToResponse(Transaction tx) {
        // Using your custom constructor:
        // Order: id, sender, receiver, amount, type, status, timestamp, note
        return new TransactionResponse(
                tx.getId(),
                tx.getSender().getEmail(),
                tx.getReceiver().getEmail(),
                tx.getAmount(),
                "SEND",           // Defaulting type as String
                "SUCCESS",        // Defaulting status as String
                tx.getTimestamp(),
                tx.getNote()
        );

    }
    @Override
    @Transactional
    public void addMoney(String email, BigDecimal amount) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the balance
        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        // Record this as a special transaction type
        Transaction topUp = new Transaction();
        topUp.setReceiver(user); // You are the receiver
        topUp.setSender(user);   // You are also the "sender" for a top-up
        topUp.setAmount(amount);
        topUp.setNote("Wallet Top-up via Bank/Card");
        topUp.setTimestamp(LocalDateTime.now());

        transactionRepository.save(topUp);

        // Notify the user
        notificationService.sendNotification(user,
                "Successfully added $" + amount + " to your wallet.",
                Notification.NotificationType.TRANSACTION);
    }

}