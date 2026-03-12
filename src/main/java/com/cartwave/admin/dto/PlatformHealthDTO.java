package com.cartwave.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlatformHealthDTO {
    private long totalUsers;
    private long totalStores;
    private long totalOrders;
    private long pendingDisputes;
    private long pendingEmails;
    private long activeSubscriptions;
    private String status;
}
