package com.firstVersion.RevPay.service;

import com.firstVersion.RevPay.dto.MoneyRequestDTO;
import com.firstVersion.RevPay.entity.MoneyRequest;
import java.util.List;

public interface MoneyRequestService {
    MoneyRequest createRequest(String requesterEmail, MoneyRequestDTO dto);
    void acceptRequest(Long requestId);
    void declineRequest(Long requestId);
    List<MoneyRequest> getMyIncomingRequests(String email);
}