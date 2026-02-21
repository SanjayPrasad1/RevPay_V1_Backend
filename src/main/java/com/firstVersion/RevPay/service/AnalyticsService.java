package com.firstVersion.RevPay.service;

import com.firstVersion.RevPay.dto.DashboardDTO;

public interface AnalyticsService {
    DashboardDTO getUserDashboard(String email);
}