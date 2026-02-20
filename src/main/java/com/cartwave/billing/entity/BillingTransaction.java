package com.cartwave.billing.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "billing_transactions", indexes = {
        @Index(name = "idx_billing_store_id", columnList = "store_id"),
        @Index(name = "idx_billing_status", columnList = "status"),
        @Index(name = "idx_billing_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BillingTransaction extends BaseEntity {

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BillingStatus status;

    @Column(length = 100)
    private String paymentMethod;

    @Column(length = 50)
    private String paymentProvider;

    @Column(columnDefinition = "TEXT")
    private String transactionDetails;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column
    private Long processedAt;

}
