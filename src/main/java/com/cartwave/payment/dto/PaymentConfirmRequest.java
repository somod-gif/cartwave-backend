package com.cartwave.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentConfirmRequest {

    @NotBlank
    private String transactionId;

    @NotBlank
    private String status; // e.g. SUCCESS, FAILED

    private String providerReference;
}
