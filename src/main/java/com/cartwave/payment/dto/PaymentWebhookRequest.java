package com.cartwave.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentWebhookRequest {

    @NotBlank
    private String transactionId;

    @NotBlank
    private String status;

    private String failureReason;
}
