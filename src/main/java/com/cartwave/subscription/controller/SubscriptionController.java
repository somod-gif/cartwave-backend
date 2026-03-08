package com.cartwave.subscription.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.subscription.dto.SubscriptionChangeRequest;
import com.cartwave.subscription.dto.SubscriptionDTO;
import com.cartwave.subscription.dto.SubscriptionPlanDTO;
import com.cartwave.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> getCurrentSubscription() {
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscriptionService.getSubscriptionForStore()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> getSubscription() {
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscriptionService.getSubscriptionForStore()));
    }

    @GetMapping("/plans")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDTO>>> listPlans() {
        return ResponseEntity.ok(ApiResponse.success("Plans retrieved successfully", subscriptionService.listPlans()));
    }

    @PostMapping("/change")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> changePlan(@Valid @RequestBody SubscriptionChangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription changed successfully", subscriptionService.changePlan(request)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> createOrUpdate(@Valid @RequestBody SubscriptionChangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", subscriptionService.changePlan(request)));
    }
}
