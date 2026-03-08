package com.cartwave.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {

    private UUID id;
    private UUID storeId;
    private String planName;
    private String status;
    private BigDecimal amount;
    private String billingCycle;
    private Boolean autoRenewal;
    private Long startDate;
    private Long endDate;
    private Long renewalDate;
    private String features;
    private UUID planId;
    private Instant createdAt;

}
