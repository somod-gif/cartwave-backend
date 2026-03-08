package com.cartwave.payment.entity;

import com.cartwave.common.entity.BaseEntity;
import com.cartwave.order.entity.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_store_id", columnList = "store_id"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, unique = true, length = 64)
    private String transactionId;

    @Column(length = 100)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    @Column(length = 100)
    private String paymentMethod;

    @Column(length = 50)
    private String paymentProvider;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column
    private Long confirmedAt;
}
