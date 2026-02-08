package com.revpay.repository;

import com.revpay.entity.MoneyRequest;
import com.revpay.enums.MoneyRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoneyRequestRepository extends JpaRepository<MoneyRequest, Long> {

    Page<MoneyRequest> findByRequesterId(Long requesterId, Pageable pageable);
    Page<MoneyRequest> findByPayerId(Long payerId, Pageable pageable);
    List<MoneyRequest> findByPayerIdAndStatus(Long payerId, MoneyRequestStatus status);
    long countByPayerIdAndStatus(Long payerId, MoneyRequestStatus status);
}