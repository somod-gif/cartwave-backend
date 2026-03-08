package com.cartwave.payment.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.payment.dto.PaymentInitiateRequest;
import com.cartwave.payment.dto.PaymentResponse;
import com.cartwave.payment.dto.PaymentWebhookRequest;
import com.cartwave.payment.dto.PaymentConfirmRequest;
import com.cartwave.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiate(@Valid @RequestBody PaymentInitiateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment initiated successfully", paymentService.initiate(request)));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirm(@Valid @RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed successfully", paymentService.confirm(request)));
    }

    @PostMapping("/webhook")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> webhook(@Valid @RequestBody PaymentWebhookRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment webhook processed successfully", paymentService.webhook(request)));
    }
}
