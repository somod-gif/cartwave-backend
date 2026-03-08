package com.cartwave.superadmin.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.dashboard.dto.SuperAdminDashboardResponse;
import com.cartwave.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SuperAdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard data", dashboardService.getSuperAdminDashboard()));
    }

    @GetMapping("/system-stats")
    public ResponseEntity<ApiResponse<SuperAdminDashboardResponse>> getSystemStats() {
        return ResponseEntity.ok(ApiResponse.success("System stats data", dashboardService.getSuperAdminDashboard()));
    }
}
