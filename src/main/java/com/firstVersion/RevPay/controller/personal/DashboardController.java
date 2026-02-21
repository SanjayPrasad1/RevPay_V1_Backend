package com.firstVersion.RevPay.controller.personal;

import com.firstVersion.RevPay.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personal/dashboard")
public class DashboardController {

    @Autowired private AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(Authentication auth) {
        return ResponseEntity.ok(analyticsService.getUserDashboard(auth.getName()));
    }
}