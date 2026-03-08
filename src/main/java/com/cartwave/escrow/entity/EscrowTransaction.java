package com.cartwave.escrow.entity;

import com.cartwave.common.entity.BaseEntity;
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
@Table(name = "escrow_transactions", indexes = {
        @Index(name = "idx_escrow_order_id", columnList = "order_id"),
        @Index(name = "idx_escrow_status", columnList = "status"),
        @Index(name = "idx_escrow_store_id", columnList = "store_id"),
        @Index(name = "idx_escrow_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EscrowTransaction extends BaseEntity {

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal holdAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EscrowStatus status;

    @Column
    private Long releaseAt;

    @Column(length = 64)
    private String transactionRef;
}
