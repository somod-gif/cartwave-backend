package com.cartwave.analytics.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "kpi_snapshots", indexes = {
        @Index(name = "idx_kpi_snapshots_store_date", columnList = "store_id,snapshot_date"),
        @Index(name = "idx_kpi_snapshots_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KpiSnapshot extends BaseEntity {

    @Column
    private UUID storeId;

    @Column(nullable = false, length = 32)
    private String scope;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal revenue;

    @Column(nullable = false)
    private Long orderCount;

    @Column(nullable = false)
    private Long customerCount;

    @Column(nullable = false)
    private LocalDate snapshotDate;
}
