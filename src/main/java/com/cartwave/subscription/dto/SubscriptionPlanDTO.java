package com.cartwave.subscription.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SubscriptionPlanDTO {

    private UUID id;
    private String name;
    private String description;
    private Integer productLimit;
    private Integer staffLimit;
    private Boolean paymentsEnabled;
    private Boolean customDomainEnabled;
    private BigDecimal price;
    private Boolean active;
}
