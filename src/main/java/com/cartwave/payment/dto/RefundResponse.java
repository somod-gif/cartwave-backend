package com.cartwave.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RefundResponse {

    private String transactionId;
    private UUID orderId;
    private BigDecimal refundedAmount;
    private String status;
    private String reason;
    private Instant refundedAt;
}
