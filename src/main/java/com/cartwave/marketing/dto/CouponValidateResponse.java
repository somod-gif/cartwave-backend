package com.cartwave.marketing.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CouponValidateResponse {

    private boolean valid;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal finalOrderAmount;
    private String message;
}
