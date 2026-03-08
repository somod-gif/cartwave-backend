package com.cartwave.billing.service;

import com.cartwave.billing.dto.BillingTransactionDTO;
import com.cartwave.billing.entity.BillingTransaction;
import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.exception.BusinessException;
import com.cartwave.subscription.service.SubscriptionService;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final BillingTransactionRepository billingTransactionRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public List<BillingTransactionDTO> getTransactionsForStore() {
        log.info("Fetching billing transactions for store");
        var storeId = TenantContext.getTenantId();

        if (!subscriptionService.isFeatureEnabled(storeId, "payments")) {
            throw new BusinessException("PAYMENTS_NOT_ALLOWED", "Current subscription plan does not allow billing/payments features.");
        }

        return billingTransactionRepository.findByStoreId(storeId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public BillingTransactionDTO toDto(BillingTransaction transaction) {
        return BillingTransactionDTO.builder()
                .id(transaction.getId())
                .storeId(transaction.getStoreId())
                .orderId(transaction.getOrderId())
                .transactionId(transaction.getTransactionId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus().name())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentProvider(transaction.getPaymentProvider())
                .transactionDetails(transaction.getTransactionDetails())
                .failureReason(transaction.getFailureReason())
                .processedAt(transaction.getProcessedAt())
                .releaseAt(transaction.getReleaseAt())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
