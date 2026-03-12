package com.cartwave.superadmin.controller;

import com.cartwave.admin.dto.CreateAdminRequest;
import com.cartwave.admin.dto.CreatePlanRequest;
import com.cartwave.admin.dto.PlatformHealthDTO;
import com.cartwave.admin.dto.RevenueSummaryDTO;
import com.cartwave.admin.dto.UserAdminDTO;
import com.cartwave.admin.service.AdminService;
import com.cartwave.common.dto.ApiResponse;
import com.cartwave.dashboard.dto.SuperAdminDashboardResponse;
import com.cartwave.dashboard.service.DashboardService;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.service.StoreService;
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
@RequestMapping("/api/v1/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin", description = "Full platform control — stores, users, subscriptions, revenue, health")
public class SuperAdminController {

    private final DashboardService dashboardService;
    private final AdminService adminService;
    private final StoreService storeService;

    // ── Overview ──────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @Operation(summary = "Platform-wide dashboard overview")
    public ResponseEntity<ApiResponse<SuperAdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard data", dashboardService.getSuperAdminDashboard()));
    }

    @GetMapping("/system-stats")
    @Operation(summary = "Platform system statistics (alias for dashboard)")
    public ResponseEntity<ApiResponse<SuperAdminDashboardResponse>> getSystemStats() {
        return ResponseEntity.ok(ApiResponse.success("System stats", dashboardService.getSuperAdminDashboard()));
    }

    @GetMapping("/health")
    @Operation(summary = "Platform health indicators — pending emails, open disputes, active subscriptions")
    public ResponseEntity<ApiResponse<PlatformHealthDTO>> getPlatformHealth() {
        return ResponseEntity.ok(ApiResponse.success("Platform health", adminService.getPlatformHealth()));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Total and monthly revenue summary")
    public ResponseEntity<ApiResponse<RevenueSummaryDTO>> getRevenueSummary() {
        return ResponseEntity.ok(ApiResponse.success("Revenue summary", adminService.getRevenueSummary()));
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "List every user on the platform")
    public ResponseEntity<ApiResponse<List<UserAdminDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", adminService.getAllUsers()));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get a user by ID")
    public ResponseEntity<ApiResponse<UserAdminDTO>> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(userId)));
    }

    @PutMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspend a user account")
    public ResponseEntity<ApiResponse<UserAdminDTO>> suspendUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User suspended", adminService.suspendUser(userId)));
    }

    @PutMapping("/users/{userId}/activate")
    @Operation(summary = "Re-activate a suspended user account")
    public ResponseEntity<ApiResponse<UserAdminDTO>> activateUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User activated", adminService.activateUser(userId)));
    }

    // ── Internal Admin Management ─────────────────────────────────────────────

    @PostMapping("/admins")
    @Operation(summary = "Create an internal CartWave admin account",
               description = "Admins are CartWave team members with platform-wide access. Only SUPER_ADMIN can create them.")
    public ResponseEntity<ApiResponse<UserAdminDTO>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request) {
        UserAdminDTO created = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin account created", created));
    }

    @GetMapping("/admins")
    @Operation(summary = "List all internal CartWave admin accounts")
    public ResponseEntity<ApiResponse<List<UserAdminDTO>>> listAdmins() {
        return ResponseEntity.ok(ApiResponse.success("Admins fetched", adminService.listAdmins()));
    }

    @DeleteMapping("/admins/{adminId}")
    @Operation(summary = "Remove an internal admin account",
               description = "Soft-deletes and suspends the admin account. Cannot be used on SUPER_ADMIN accounts.")
    public ResponseEntity<ApiResponse<Void>> removeAdmin(@PathVariable UUID adminId) {
        adminService.removeAdmin(adminId);
        return ResponseEntity.ok(ApiResponse.success("Admin account removed", null));
    }

    // ── Stores ────────────────────────────────────────────────────────────────

    @GetMapping("/stores")
    @Operation(summary = "List all stores on the platform")
    public ResponseEntity<ApiResponse<List<StoreDTO>>> getAllStores() {
        return ResponseEntity.ok(ApiResponse.success("Stores fetched", storeService.getAllStores()));
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
