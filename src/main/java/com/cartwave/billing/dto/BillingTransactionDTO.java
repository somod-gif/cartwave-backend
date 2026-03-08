package com.cartwave.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingTransactionDTO {

    private UUID id;
    private UUID storeId;
    private UUID orderId;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String paymentProvider;
    private String transactionDetails;
    private String failureReason;
    private Long processedAt;
    private Long releaseAt;
    private Instant createdAt;

}
