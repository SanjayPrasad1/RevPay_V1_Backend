package com.firstVersion.RevPay.repository;

import com.firstVersion.RevPay.entity.MoneyRequest;
import com.firstVersion.RevPay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Entity comes first (MoneyRequest), then the ID type (Long)
public interface MoneyRequestRepository extends JpaRepository<MoneyRequest, Long> {

    List<MoneyRequest> findByTargetUserAndStatus(User targetUser, MoneyRequest.RequestStatus status);
}