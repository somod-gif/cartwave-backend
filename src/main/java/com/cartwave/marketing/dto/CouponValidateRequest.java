package com.cartwave.marketing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CouponValidateRequest {

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.01")
    private BigDecimal orderAmount;
}
