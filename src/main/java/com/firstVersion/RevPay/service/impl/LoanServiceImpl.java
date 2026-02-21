package com.firstVersion.RevPay.service.impl;

import com.firstVersion.RevPay.dto.LoanRequest;
import com.firstVersion.RevPay.entity.Loan;
import com.firstVersion.RevPay.entity.User;
import com.firstVersion.RevPay.repository.LoanRepository;
import com.firstVersion.RevPay.repository.UserRepository;
import com.firstVersion.RevPay.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Loan applyForLoan(String email, LoanRequest request) {
        User business = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Business user not found"));

        Loan loan = new Loan();
        loan.setBusiness(business);
        loan.setPrincipalAmount(request.getAmount());
        loan.setPurpose(request.getPurpose());
        loan.setTenureMonths(request.getTenureMonths());
        loan.setStatus(Loan.LoanStatus.PENDING);
        loan.setAppliedAt(LocalDateTime.now());

        return loanRepository.save(loan);
    }

    @Override
    public List<Loan> getMyLoans(String email) {
        User business = userRepository.findByEmail(email).orElseThrow();
        return loanRepository.findByBusiness(business);
    }

    @Override
    public List<Loan> getAllPendingLoans() {
        return loanRepository.findByStatus(Loan.LoanStatus.PENDING);
    }

    @Override
    public void updateLoanStatus(Long loanId, String status) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(Loan.LoanStatus.valueOf(status.toUpperCase()));
        loanRepository.save(loan);
    }
}