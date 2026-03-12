package com.cartwave.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {

    private String transactionId;
    private String status;
    private String paymentProvider;
    private String paymentMethod;
    /** Paystack redirect URL — present only when provider is PAYSTACK */
    private String authorizationUrl;
}
