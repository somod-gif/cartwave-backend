package com.cartwave.order.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OrderTrackingDTO {
    private UUID id;
    private UUID orderId;
    private String status;
    private String note;
    private UUID updatedBy;
    private Instant timestamp;
}
