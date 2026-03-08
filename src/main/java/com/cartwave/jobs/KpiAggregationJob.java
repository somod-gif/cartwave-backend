package com.cartwave.jobs;

import com.cartwave.analytics.entity.KpiSnapshot;
import com.cartwave.analytics.repository.KpiSnapshotRepository;
import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.customer.repository.CustomerRepository;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiAggregationJob {

    private final StoreRepository storeRepository;
    private final KpiSnapshotRepository kpiSnapshotRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final BillingTransactionRepository billingTransactionRepository;

    @Scheduled(cron = "${cartwave.jobs.kpi-aggregation-cron:0 */30 * * * *}")
    public void run() {
        LocalDate today = LocalDate.now();
        for (Store store : storeRepository.findAllActive()) {
            KpiSnapshot snapshot = kpiSnapshotRepository.findByStoreIdAndSnapshotDateAndDeletedFalse(store.getId(), today)
                    .orElseGet(KpiSnapshot::new);
            snapshot.setStoreId(store.getId());
            snapshot.setScope("STORE");
            snapshot.setRevenue(defaultAmount(billingTransactionRepository.sumCapturedForStore(store.getId())));
            snapshot.setOrderCount(orderRepository.countByStoreIdAndDeletedFalse(store.getId()));
            snapshot.setCustomerCount(customerRepository.countByStoreIdAndDeletedFalse(store.getId()));
            snapshot.setSnapshotDate(today);
            kpiSnapshotRepository.save(snapshot);
        }
        log.info("KPI aggregation cycle executed");
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
