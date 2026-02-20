package com.cartwave.subscription.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscriptions_store_id", columnList = "store_id"),
        @Index(name = "idx_subscriptions_status", columnList = "status"),
        @Index(name = "idx_subscriptions_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Subscription extends BaseEntity {

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 100)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubscriptionStatus status;

    @Column
    private Long startDate;

    @Column
    private Long endDate;

    @Column
    private Long renewalDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String billingCycle;

    @Builder.Default
    @Column(nullable = false)
    private Boolean autoRenewal = true;

    @Column(columnDefinition = "TEXT")
    private String features;

}
