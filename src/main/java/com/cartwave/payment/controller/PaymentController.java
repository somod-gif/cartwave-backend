package com.cartwave.payment.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.payment.dto.PaymentInitiateRequest;
import com.cartwave.payment.dto.PaymentResponse;
import com.cartwave.payment.dto.PaymentWebhookRequest;
import com.cartwave.payment.dto.PaymentConfirmRequest;
import com.cartwave.payment.dto.RefundRequest;
import com.cartwave.payment.dto.RefundResponse;
import com.cartwave.payment.service.PaymentService;
import com.cartwave.payment.service.PaystackService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PaystackService paystackService;
    private final ObjectMapper objectMapper;

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
    public ResponseEntity<ApiResponse<PaymentResponse>> webhook(@Valid @RequestBody PaymentWebhookRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment webhook processed successfully", paymentService.webhook(request)));
    }

    /**
     * Paystack-specific webhook endpoint.
     *
     * <p>This endpoint is publicly accessible (permitAll in SecurityConfig) so
     * Paystack can POST to it without a JWT. Security is provided by HMAC-SHA512
     * signature verification using the Paystack secret key.
     *
     * <p>Paystack sends the raw JSON body and a signature in the
     * {@code X-Paystack-Signature} header.
     */
    @PostMapping("/paystack/webhook")
    public ResponseEntity<Void> paystackWebhook(HttpServletRequest httpRequest) {
        try {
            byte[] rawBody = httpRequest.getInputStream().readAllBytes();
            String signature = httpRequest.getHeader("X-Paystack-Signature");

            if (!paystackService.verifyWebhookSignature(rawBody, signature)) {
                log.warn("Paystack webhook: invalid signature from IP={}", httpRequest.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            JsonNode payload = objectMapper.readTree(rawBody);
            String event = payload.path("event").asText("");

            // Only process charge events
            if (!event.startsWith("charge.")) {
                return ResponseEntity.ok().build();
            }

            JsonNode data = payload.path("data");
            String reference = data.path("reference").asText(null);
            String status = data.path("status").asText("failed");
            String gatewayRef = data.path("id").asText(reference);

            if (reference == null || reference.isBlank()) {
                log.warn("Paystack webhook: missing reference in payload");
                return ResponseEntity.badRequest().build();
            }

            paymentService.processPaystackWebhook(reference, status, gatewayRef);
            return ResponseEntity.ok().build();

        } catch (IOException ex) {
            log.error("Paystack webhook: failed to read/parse body: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ── Refund ────────────────────────────────────────────────────────────────

    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RefundResponse>> refund(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", paymentService.refund(request)));
    }
}

