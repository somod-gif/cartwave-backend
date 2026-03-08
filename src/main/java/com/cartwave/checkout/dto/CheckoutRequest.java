package com.cartwave.checkout.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank
    private String deliveryAddress;

    private String customerEmail;
    private String customerPhoneNumber;
    private String notes;
    private String paymentMethod;
    private String paymentProvider;
}
