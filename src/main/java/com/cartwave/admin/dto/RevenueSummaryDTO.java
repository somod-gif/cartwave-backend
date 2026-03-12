package com.cartwave.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RevenueSummaryDTO {
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private long totalTransactions;
    private long totalOrders;
    private long totalStores;
    private long totalUsers;
    private long activeSubscriptions;
}
