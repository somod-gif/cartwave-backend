package com.cartwave.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminDashboardResponse {

    private long productCount;
    private long orderCount;
    private long pendingOrders;
    private long lowStockProducts;
    private long staffCount;
    private long unresolvedFraudFlags;
    private BigDecimal revenue;
}
