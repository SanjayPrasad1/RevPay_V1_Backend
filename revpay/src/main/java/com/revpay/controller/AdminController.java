package com.revpay.controller;

import com.revpay.common.ApiResponse;
import com.revpay.dto.admin.AdminUserResponse;
import com.revpay.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.revpay.dto.loan.LoanResponse;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Users",
                adminService.getAllUsers(
                        PageRequest.of(page, size,
                                Sort.by("createdAt").descending()))));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User",
                adminService.getUserById(id)));
    }

    @PostMapping("/users/{id}/toggle")
    public ResponseEntity<ApiResponse<AdminUserResponse>> toggleUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                adminService.toggleUserStatus(id)));
    }
    @GetMapping("/loans")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getLoans(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Loans",
                adminService.getAllLoans(status,
                        PageRequest.of(page, size,
                                Sort.by("createdAt").descending()))));
    }

    @PostMapping("/loans/{id}/approve")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Loan approved",
                adminService.approveLoan(id)));
    }

    @PostMapping("/loans/{id}/reject")
    public ResponseEntity<ApiResponse<LoanResponse>> rejectLoan(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok("Loan rejected",
                adminService.rejectLoan(id,
                        body.getOrDefault("reason", "Rejected by admin"))));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        var stats = new java.util.HashMap<String, Object>();
        stats.put("totalUsers", adminService.getTotalUsers());
        stats.put("personalUsers", adminService.getTotalByRole("PERSONAL"));
        stats.put("businessUsers", adminService.getTotalByRole("BUSINESS"));
        stats.put("pendingLoans", adminService.getPendingLoansCount());
        return ResponseEntity.ok(ApiResponse.ok("Stats", stats));
    }
}