package com.cartwave.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReviewDTO {

    private UUID id;
    private UUID productId;
    private UUID customerId;
    private UUID storeId;

    @NotNull
    @Min(1)
    @Max(5)
    private Short rating;

    private String comment;
    private Boolean verified;
    private Instant createdAt;
}
