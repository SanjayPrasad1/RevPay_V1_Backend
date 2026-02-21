package com.firstVersion.RevPay.service;

import com.firstVersion.RevPay.dto.TransactionRequest;
import com.firstVersion.RevPay.dto.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    TransactionResponse sendMoney(String senderEmail, TransactionRequest request);
    List<TransactionResponse> getTransactionHistory(String email);
    void addMoney(String email, BigDecimal amount);
}