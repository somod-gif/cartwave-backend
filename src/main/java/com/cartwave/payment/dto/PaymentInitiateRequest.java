package com.cartwave.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentInitiateRequest {

    @NotNull
    private UUID orderId;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String paymentProvider;
}
