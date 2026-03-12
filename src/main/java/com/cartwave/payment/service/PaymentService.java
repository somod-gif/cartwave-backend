package com.cartwave.payment.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cartwave.billing.entity.BillingStatus;
import com.cartwave.billing.entity.BillingTransaction;
import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.escrow.service.EscrowService;
import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.order.entity.Order;
import com.cartwave.order.entity.PaymentStatus;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.payment.dto.PaymentConfirmRequest;
import com.cartwave.payment.dto.PaymentInitiateRequest;
import com.cartwave.payment.dto.PaymentResponse;
import com.cartwave.payment.dto.PaymentWebhookRequest;
import com.cartwave.payment.dto.RefundRequest;
import com.cartwave.payment.dto.RefundResponse;
import com.cartwave.payment.entity.Payment;
import com.cartwave.payment.repository.PaymentRepository;
import com.cartwave.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final BillingTransactionRepository billingTransactionRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final EscrowService escrowService;

    public PaymentResponse initiate(PaymentInitiateRequest request) {
        var storeId = TenantContext.getTenantId();
        Order order = orderRepository.findByIdAndStoreId(request.getOrderId(), storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        BillingTransaction transaction = billingTransactionRepository.findFirstByOrderIdAndStoreId(order.getId(), storeId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_READY", "Checkout must create a billing transaction before payment initiation."));

        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setPaymentProvider(request.getPaymentProvider());
        transaction.setStatus(BillingStatus.PROCESSING);
        transaction.setTransactionDetails("payment-initiated");
        order.setPaymentStatus(PaymentStatus.PROCESSING);

        billingTransactionRepository.save(transaction);
        orderRepository.save(order);

        return PaymentResponse.builder()
                .transactionId(transaction.getTransactionId())
                .status(transaction.getStatus().name())
                .paymentProvider(transaction.getPaymentProvider())
                .paymentMethod(transaction.getPaymentMethod())
                .build();
    }

    public PaymentResponse confirm(PaymentConfirmRequest request) {
        BillingTransaction transaction = billingTransactionRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("BillingTransaction", "transactionId", request.getTransactionId()));
        Order order = orderRepository.findByIdAndStoreId(transaction.getOrderId(), transaction.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", transaction.getOrderId()));

        String status = request.getStatus().trim().toUpperCase();
        boolean success = status.equals("SUCCESS") || status.equals("COMPLETED") || status.equals("PAID");

        Payment payment = paymentRepository.findByTransactionId(transaction.getTransactionId())
                .orElseGet(() -> Payment.builder()
                        .storeId(transaction.getStoreId())
                        .orderId(order.getId())
                        .transactionId(transaction.getTransactionId())
                        .currency(transaction.getCurrency())
                        .paymentMethod(transaction.getPaymentMethod())
                        .paymentProvider(transaction.getPaymentProvider())
                        .amount(transaction.getAmount())
                        .build());
        payment.setProviderReference(request.getProviderReference());
        payment.setConfirmedAt(Instant.now().toEpochMilli());
        payment.setStatus(success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        paymentRepository.save(payment);

        if (success) {
            transaction.setStatus(BillingStatus.HOLD);
            transaction.setProcessedAt(Instant.now().toEpochMilli());
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            // create escrow hold
            long releaseAt = order.getReleaseAt() != null ? order.getReleaseAt()
                    : Instant.now().plusSeconds(2 * 24 * 60 * 60L).toEpochMilli();
            transaction.setReleaseAt(releaseAt);
            escrowService.createOrUpdateHold(transaction.getStoreId(), order.getId(), transaction.getAmount(), releaseAt, transaction.getTransactionId());
        } else {
            transaction.setStatus(BillingStatus.FAILED);
            transaction.setFailureReason("PAYMENT_CONFIRMATION_" + status);
            order.setPaymentStatus(PaymentStatus.FAILED);
        }

        billingTransactionRepository.save(transaction);
        orderRepository.save(order);

        return PaymentResponse.builder()
                .transactionId(transaction.getTransactionId())
                .status(transaction.getStatus().name())
                .paymentProvider(transaction.getPaymentProvider())
                .paymentMethod(transaction.getPaymentMethod())
                .build();
    }

    public PaymentResponse webhook(PaymentWebhookRequest request) {
        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest();
        confirmRequest.setTransactionId(request.getTransactionId());
        confirmRequest.setStatus(request.getStatus());
        confirmRequest.setProviderReference(request.getFailureReason());
        return confirm(confirmRequest);
    }

    // ── Refund ────────────────────────────────────────────────────────────────

    public RefundResponse refund(RefundRequest request) {
        var storeId = TenantContext.getTenantId();

        BillingTransaction transaction = billingTransactionRepository
                .findByTransactionIdAndStoreId(request.getTransactionId(), storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionId", request.getTransactionId()));

        // Only COMPLETED or HOLD transactions can be refunded
        if (transaction.getStatus() != BillingStatus.COMPLETED && transaction.getStatus() != BillingStatus.HOLD) {
            throw new BusinessException("REFUND_NOT_ELIGIBLE",
                    "Only COMPLETED or HOLD transactions can be refunded. Current status: " + transaction.getStatus());
        }

        // Determine refund amount (partial or full)
        var refundAmount = request.getAmount() != null ? request.getAmount() : transaction.getAmount();
        if (refundAmount.compareTo(transaction.getAmount()) > 0) {
            throw new BusinessException("REFUND_EXCEEDS_AMOUNT",
                    "Refund amount cannot exceed the original transaction amount of " + transaction.getAmount());
        }

        // Mark transaction as refunded
        transaction.setStatus(BillingStatus.REFUNDED);
        transaction.setFailureReason(request.getReason());
        billingTransactionRepository.save(transaction);

        // Update order payment status
        if (transaction.getOrderId() != null) {
            orderRepository.findById(transaction.getOrderId()).ifPresent(order -> {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                orderRepository.save(order);
            });
        }

        return RefundResponse.builder()
                .transactionId(transaction.getTransactionId())
                .orderId(transaction.getOrderId())
                .refundedAmount(refundAmount)
                .status("REFUNDED")
                .reason(request.getReason())
                .refundedAt(Instant.now())
                .build();
    }
}
