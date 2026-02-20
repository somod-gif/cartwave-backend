package com.cartwave.subscription.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans", indexes = {
        @Index(name = "idx_subscription_plans_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubscriptionPlan extends BaseEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 0 means unlimited */
    @Column
    private Integer productLimit;

    /** 0 means unlimited */
    @Column
    private Integer staffLimit;

    @Column
    private Boolean paymentsEnabled;

    @Column
    private Boolean customDomainEnabled;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

}

