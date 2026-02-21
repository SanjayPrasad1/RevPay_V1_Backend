package com.firstVersion.RevPay.service;

import com.firstVersion.RevPay.dto.LoanRequest;
import com.firstVersion.RevPay.entity.Loan;
import java.util.List;

public interface LoanService {
    Loan applyForLoan(String email, LoanRequest request);
    List<Loan> getMyLoans(String email);
    List<Loan> getAllPendingLoans(); // For admin/system review
    void updateLoanStatus(Long loanId, String status);
}