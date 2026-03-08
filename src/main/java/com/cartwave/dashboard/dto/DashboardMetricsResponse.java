package com.cartwave.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardMetricsResponse {
    private long totalOrders;
    private BigDecimal revenue;
    private long activeCustomers;
    private long pendingEscrow;
}
