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
import com.cartwave.tenant.TenantContext;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.repository.UserRepository;
import com.cartwave.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
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

    public DashboardMetricsResponse getMetrics() {
        var storeId = TenantContext.getTenantId();
        return DashboardMetricsResponse.builder()
                .totalOrders(orderRepository.countByStoreIdAndDeletedFalse(storeId))
                .revenue(defaultAmount(orderRepository.sumRevenueForStore(storeId)))
                .activeCustomers(customerRepository.countByStoreIdAndDeletedFalse(storeId))
                .pendingEscrow(escrowTransactionRepository.countByStoreIdAndStatusAndDeletedFalse(storeId, EscrowStatus.HELD))
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
