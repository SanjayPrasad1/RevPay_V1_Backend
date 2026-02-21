package com.firstVersion.RevPay.repository;

import com.firstVersion.RevPay.entity.Loan;
import com.firstVersion.RevPay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBusiness(User business);
    List<Loan> findByStatus(Loan.LoanStatus status);
}