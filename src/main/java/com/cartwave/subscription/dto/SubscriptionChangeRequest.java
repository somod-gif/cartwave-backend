package com.cartwave.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionChangeRequest {

    @NotBlank
    private String planName;

    private String billingCycle;
    private Boolean autoRenewal;
}
