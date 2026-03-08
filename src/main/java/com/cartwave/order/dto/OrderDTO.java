package com.cartwave.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private UUID id;
    private UUID storeId;
    private String orderNumber;
    private UUID customerId;
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String status;
    private String paymentStatus;
    private String deliveryAddress;
    private String customerEmail;
    private String customerPhoneNumber;
    private String notes;
    private Long completedAt;
    private Long releaseAt;
    private Instant createdAt;

}
