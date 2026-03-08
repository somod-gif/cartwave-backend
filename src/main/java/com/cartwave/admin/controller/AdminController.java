package com.cartwave.admin.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.dashboard.dto.AdminDashboardResponse;
import com.cartwave.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_OWNER', 'STAFF')")
public class AdminController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard data", dashboardService.getAdminDashboard()));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats data", dashboardService.getAdminDashboard()));
    }
}
