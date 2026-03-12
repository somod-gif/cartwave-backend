package com.cartwave.marketing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    /** PERCENT or FIXED */
    @NotBlank(message = "Discount type is required (PERCENT or FIXED)")
    private String discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    private BigDecimal minOrderValue;

    private Integer maxUses;

    private Instant expiresAt;
}
