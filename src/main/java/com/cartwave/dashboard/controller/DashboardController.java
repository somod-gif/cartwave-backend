package com.cartwave.dashboard.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.dashboard.dto.DashboardMetricsResponse;
import com.cartwave.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN','STAFF','SUPER_ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> metrics() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics", dashboardService.getMetrics()));
    }
}
