package com.cartwave.fraud.entity;

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

import java.util.UUID;

@Entity
@Table(name = "fraud_flags", indexes = {
        @Index(name = "idx_fraud_flags_store_id", columnList = "store_id"),
        @Index(name = "idx_fraud_flags_reviewed", columnList = "reviewed"),
        @Index(name = "idx_fraud_flags_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FraudFlag extends BaseEntity {

    @Column(nullable = false)
    private UUID storeId;

    @Column
    private UUID orderId;

    @Column
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FraudSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private Boolean reviewed;
}
