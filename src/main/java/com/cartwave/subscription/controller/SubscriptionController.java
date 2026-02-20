package com.cartwave.subscription.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.subscription.dto.SubscriptionDTO;
import com.cartwave.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> getCurrentSubscription() {
        log.info("Get current subscription endpoint called");
        SubscriptionDTO subscriptionDTO = subscriptionService.getSubscriptionForStore();
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscriptionDTO));
    }

}
