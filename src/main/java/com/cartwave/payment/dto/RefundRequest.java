package com.cartwave.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {

    @NotBlank(message = "transactionId is required")
    private String transactionId;

    /** Optional: for partial refund. If null, full amount is refunded. */
    @DecimalMin(value = "0.01", message = "Refund amount must be positive")
    private BigDecimal amount;

    private String reason;
}
