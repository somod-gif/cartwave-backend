package com.cartwave.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class WishlistItemDTO {
    private UUID id;
    private UUID productId;
    private UUID storeId;
    private Instant savedAt;
}
