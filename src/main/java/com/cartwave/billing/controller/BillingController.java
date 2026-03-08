package com.cartwave.billing.controller;

import com.cartwave.billing.dto.BillingTransactionDTO;
import com.cartwave.billing.service.BillingService;
import com.cartwave.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<BillingTransactionDTO>>> getTransactions() {
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", billingService.getTransactionsForStore()));
    }
}
