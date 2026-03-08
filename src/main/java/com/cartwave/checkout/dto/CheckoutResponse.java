package com.cartwave.checkout.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CheckoutResponse {

    private UUID orderId;
    private String orderNumber;
    private UUID billingTransactionId;
    private String transactionId;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
}
