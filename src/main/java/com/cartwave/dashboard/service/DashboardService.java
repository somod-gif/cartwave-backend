package com.cartwave.dashboard.service;

import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.dashboard.dto.AdminDashboardResponse;
import com.cartwave.dashboard.dto.DashboardMetricsResponse;
import com.cartwave.dashboard.dto.SuperAdminDashboardResponse;
import com.cartwave.escrow.entity.EscrowStatus;
import com.cartwave.escrow.repository.EscrowTransactionRepository;
import com.cartwave.fraud.repository.FraudFlagRepository;
import com.cartwave.order.entity.OrderStatus;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.product.repository.ProductRepository;
import com.cartwave.staff.repository.StaffRepository;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.subscription.repository.SubscriptionRepository;
import com.cartwave.tenant.TenantContext;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.repository.UserRepository;
import com.cartwave.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StaffRepository staffRepository;
    private final FraudFlagRepository fraudFlagRepository;
    private final BillingTransactionRepository billingTransactionRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AdminDashboardResponse getAdminDashboard() {
        var storeId = TenantContext.getTenantId();
        return AdminDashboardResponse.builder()
                .productCount(productRepository.countByStoreIdAndDeletedFalse(storeId))
                .orderCount(orderRepository.countByStoreIdAndDeletedFalse(storeId))
                .pendingOrders(orderRepository.countByStatusAndStoreIdAndDeletedFalse(OrderStatus.PENDING, storeId))
                .lowStockProducts(productRepository.countByStoreIdAndStockLessThanEqualAndDeletedFalse(storeId, 5L))
                .staffCount(staffRepository.countByStoreIdAndDeletedFalse(storeId))
                .unresolvedFraudFlags(fraudFlagRepository.countByStoreIdAndReviewedFalseAndDeletedFalse(storeId))
                .revenue(defaultAmount(orderRepository.sumRevenueForStore(storeId)))
                .build();
    }

    @Cacheable(value = "dashboard-metrics", key = "T(com.cartwave.tenant.TenantContext).getTenantId()")
    public DashboardMetricsResponse getMetrics() {
        var storeId = TenantContext.getTenantId();

        // Subscription info
        String planName = null;
        String planStatus = null;
        try {
            var subOpt = subscriptionRepository.findByStoreId(storeId);
            if (subOpt.isPresent()) {
                planName = subOpt.get().getPlanName();
                planStatus = subOpt.get().getStatus() == null ? null : subOpt.get().getStatus().name();
            }
        } catch (Exception ignored) {}

        return DashboardMetricsResponse.builder()
                // existing
                .totalOrders(orderRepository.countByStoreIdAndDeletedFalse(storeId))
                .revenue(defaultAmount(orderRepository.sumRevenueForStore(storeId)))
                .activeCustomers(customerRepository.countByStoreIdAndDeletedFalse(storeId))
                .pendingEscrow(escrowTransactionRepository.countByStoreIdAndStatusAndDeletedFalse(storeId, EscrowStatus.HELD))
                // V2 enrichment
                .pendingOrders(orderRepository.countByStatusAndStoreIdAndDeletedFalse(OrderStatus.PENDING, storeId))
                .deliveredOrders(orderRepository.countByStatusAndStoreIdAndDeletedFalse(OrderStatus.DELIVERED, storeId))
                .productCount(productRepository.countByStoreIdAndDeletedFalse(storeId))
                .lowStockProducts(productRepository.countByStoreIdAndStockLessThanEqualAndDeletedFalse(storeId, 5L))
                .subscriptionPlan(planName)
                .subscriptionStatus(planStatus)
                .build();
    }

    public SuperAdminDashboardResponse getSuperAdminDashboard() {
        return SuperAdminDashboardResponse.builder()
                .storeCount(storeRepository.countByDeletedFalse())
                .userCount(userRepository.countByDeletedFalse())
                .ownerCount(userRepository.findByRole(UserRole.BUSINESS_OWNER).size())
                .customerCount(customerRepository.countByDeletedFalse())
                .orderCount(orderRepository.countByDeletedFalse())
                .revenue(defaultAmount(billingTransactionRepository.sumCapturedGlobal()))
                .build();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
