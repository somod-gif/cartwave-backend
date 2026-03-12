package com.cartwave.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProductVariantDTO {

    private UUID id;
    private UUID productId;

    @NotBlank
    private String variantName;

    private String sku;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @PositiveOrZero
    private Long stockQuantity;

    private String imageUrl;
    private Instant createdAt;
}
