package com.cartwave.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SuperAdminDashboardResponse {

    private long storeCount;
    private long userCount;
    private long ownerCount;
    private long customerCount;
    private long orderCount;
    private BigDecimal revenue;
}
