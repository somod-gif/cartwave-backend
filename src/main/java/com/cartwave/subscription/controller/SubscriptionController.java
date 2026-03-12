package com.cartwave.subscription.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.subscription.dto.SubscribeRequest;
import com.cartwave.subscription.dto.SubscriptionChangeRequest;
import com.cartwave.subscription.dto.SubscriptionDTO;
import com.cartwave.subscription.dto.SubscriptionPlanDTO;
import com.cartwave.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Subscriptions", description = "Store subscription management")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Get current store subscription")
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> getCurrentSubscription() {
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscriptionService.getSubscriptionForStore()));
    }

    @Operation(summary = "Get current store subscription (alias)")
    @GetMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> getSubscription() {
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscriptionService.getSubscriptionForStore()));
    }

    @Operation(summary = "List all active plans — public")
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDTO>>> listPlans() {
        return ResponseEntity.ok(ApiResponse.success("Plans retrieved successfully", subscriptionService.listPlans()));
    }

    @Operation(summary = "Subscribe / change plan")
    @PostMapping("/subscribe")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> subscribe(@Valid @RequestBody SubscribeRequest request) {
        SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
        changeRequest.setPlanName(request.getPlanName());
        changeRequest.setBillingCycle(request.getBillingCycle());
        changeRequest.setAutoRenewal(request.getAutoRenewal());
        return ResponseEntity.ok(ApiResponse.success("Subscribed successfully", subscriptionService.changePlan(changeRequest)));
    }

    @Operation(summary = "Cancel current subscription")
    @PostMapping("/cancel")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> cancel() {
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled", subscriptionService.cancelSubscription()));
    }

    @Operation(summary = "Change plan (legacy endpoint)")
    @PostMapping("/change")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> changePlan(@Valid @RequestBody SubscriptionChangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription changed successfully", subscriptionService.changePlan(request)));
    }

    @Operation(summary = "Create or update subscription (legacy)")
    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> createOrUpdate(@Valid @RequestBody SubscriptionChangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", subscriptionService.changePlan(request)));
    }
}
