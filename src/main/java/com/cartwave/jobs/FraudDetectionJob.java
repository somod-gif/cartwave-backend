package com.cartwave.jobs;

import com.cartwave.fraud.entity.FraudFlag;
import com.cartwave.fraud.entity.FraudSeverity;
import com.cartwave.fraud.repository.FraudFlagRepository;
import com.cartwave.order.entity.Order;
import com.cartwave.order.entity.PaymentStatus;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudDetectionJob {

    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final FraudFlagRepository fraudFlagRepository;

    @Scheduled(cron = "${cartwave.jobs.fraud-scan-cron:0 */10 * * * *}")
    public void run() {
        for (Store store : storeRepository.findAllActive()) {
            for (Order order : orderRepository.findByStoreId(store.getId(), Pageable.unpaged())) {
                if (order.getTotalAmount() != null
                        && order.getTotalAmount().compareTo(BigDecimal.valueOf(10000)) >= 0
                        && !fraudFlagRepository.existsByOrderIdAndReasonAndDeletedFalse(order.getId(), "HIGH_VALUE_ORDER")) {
                    fraudFlagRepository.save(FraudFlag.builder()
                            .storeId(store.getId())
                            .orderId(order.getId())
                            .customerId(order.getCustomerId())
                            .severity(FraudSeverity.MEDIUM)
                            .reason("HIGH_VALUE_ORDER")
                            .reviewed(false)
                            .build());
                }

                if (order.getPaymentStatus() == PaymentStatus.FAILED
                        && !fraudFlagRepository.existsByOrderIdAndReasonAndDeletedFalse(order.getId(), "FAILED_PAYMENT")) {
                    fraudFlagRepository.save(FraudFlag.builder()
                            .storeId(store.getId())
                            .orderId(order.getId())
                            .customerId(order.getCustomerId())
                            .severity(FraudSeverity.HIGH)
                            .reason("FAILED_PAYMENT")
                            .reviewed(false)
                            .build());
                }
            }
        }
        log.debug("FraudDetectionJob cycle executed");
    }
}
