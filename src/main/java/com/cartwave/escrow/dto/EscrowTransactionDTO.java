package com.cartwave.escrow.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class EscrowTransactionDTO {
    private UUID id;
    private UUID storeId;
    private UUID orderId;
    private BigDecimal holdAmount;
    private BigDecimal platformFeePercent;
    private BigDecimal sellerAmount;
    private String status;
    private Long releaseAt;
    private Long releasedAt;
    private String transactionRef;
    private Instant createdAt;
}
