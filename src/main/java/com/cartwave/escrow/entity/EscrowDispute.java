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

import java.util.UUID;

@Entity
@Table(name = "escrow_disputes", indexes = {
        @Index(name = "idx_escrow_disputes_escrow_id", columnList = "escrow_transaction_id"),
        @Index(name = "idx_escrow_disputes_status", columnList = "status"),
        @Index(name = "idx_escrow_disputes_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EscrowDispute extends BaseEntity {

    @Column(name = "escrow_transaction_id", nullable = false)
    private UUID escrowTransactionId;

    @Column(name = "raised_by_user_id", nullable = false)
    private UUID raisedByUserId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EscrowDisputeStatus status;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column
    private Long resolvedAt;
}
