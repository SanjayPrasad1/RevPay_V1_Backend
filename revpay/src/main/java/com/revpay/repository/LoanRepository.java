package com.revpay.repository;

import com.revpay.entity.Loan;
import com.revpay.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    Page<Loan> findByBorrowerId(Long borrowerId, Pageable pageable);
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    Page<Loan> findAll(Pageable pageable);
    long countByStatus(LoanStatus status);
    List<Loan> findByBorrowerIdAndStatus(Long borrowerId, LoanStatus status);
    long countByBorrowerIdAndStatus(Long borrowerId, LoanStatus status);
}