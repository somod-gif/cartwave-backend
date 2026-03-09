package com.cartwave.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;
    private Integer productLimit;
    private Integer staffLimit;
    private Boolean paymentsEnabled = true;
    private Boolean customDomainEnabled = false;
}
