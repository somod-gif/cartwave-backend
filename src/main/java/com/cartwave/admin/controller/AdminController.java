package com.cartwave.admin.controller;

import com.cartwave.admin.dto.CreatePlanRequest;
import com.cartwave.admin.dto.PlatformHealthDTO;
import com.cartwave.admin.dto.RevenueSummaryDTO;
import com.cartwave.admin.dto.UserAdminDTO;
import com.cartwave.admin.service.AdminService;
import com.cartwave.common.dto.ApiResponse;
import com.cartwave.dashboard.dto.AdminDashboardResponse;
import com.cartwave.dashboard.service.DashboardService;
import com.cartwave.subscription.entity.SubscriptionPlan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Admin", description = "Platform administration — users, revenue, health, and plan management")
public class AdminController {

    private final DashboardService dashboardService;
    private final AdminService adminService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard overview")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard data", dashboardService.getAdminDashboard()));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform stats (alias for dashboard)")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats data", dashboardService.getAdminDashboard()));
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "List all platform users")
    public ResponseEntity<ApiResponse<List<UserAdminDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", adminService.getAllUsers()));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get a single user by ID")
    public ResponseEntity<ApiResponse<UserAdminDTO>> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(userId)));
    }

    @PutMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspend a user account")
    public ResponseEntity<ApiResponse<UserAdminDTO>> suspendUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User suspended", adminService.suspendUser(userId)));
    }

    @PutMapping("/users/{userId}/activate")
    @Operation(summary = "Activate a suspended user account")
    public ResponseEntity<ApiResponse<UserAdminDTO>> activateUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User activated", adminService.activateUser(userId)));
    }

    // ── Revenue ───────────────────────────────────────────────────────────────

    @GetMapping("/revenue")
    @Operation(summary = "Get platform revenue summary")
    public ResponseEntity<ApiResponse<RevenueSummaryDTO>> getRevenueSummary() {
        return ResponseEntity.ok(ApiResponse.success("Revenue summary", adminService.getRevenueSummary()));
    }

    // ── Platform Health ───────────────────────────────────────────────────────

    @GetMapping("/health")
    @Operation(summary = "Get platform health indicators")
    public ResponseEntity<ApiResponse<PlatformHealthDTO>> getPlatformHealth() {
        return ResponseEntity.ok(ApiResponse.success("Platform health", adminService.getPlatformHealth()));
    }

    // ── Subscription Plans ────────────────────────────────────────────────────

    @PostMapping("/plans")
    @Operation(summary = "Create a new subscription plan")
    public ResponseEntity<ApiResponse<SubscriptionPlan>> createPlan(
            @Valid @RequestBody CreatePlanRequest request) {
        SubscriptionPlan plan = adminService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Plan created", plan));
    }

    @DeleteMapping("/plans/{planId}/deactivate")
    @Operation(summary = "Deactivate a subscription plan")
    public ResponseEntity<ApiResponse<Void>> deactivatePlan(@PathVariable UUID planId) {
        adminService.deactivatePlan(planId);
        return ResponseEntity.ok(ApiResponse.success("Plan deactivated", null));
    }
}
