package com.cartwave.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardMetricsResponse {
    // ── Existing fields ───────────────────────────────────────────────────────
    private long totalOrders;
    private BigDecimal revenue;
    private long activeCustomers;
    private long pendingEscrow;

    // ── V2 enrichment fields ──────────────────────────────────────────────────
    private long pendingOrders;
    private long deliveredOrders;
    private long productCount;
    private long lowStockProducts;
    private BigDecimal monthlyRevenue;

    /** Current plan name, e.g. "GROWTH" */
    private String subscriptionPlan;

    /** Subscription status, e.g. "ACTIVE" */
    private String subscriptionStatus;
}
