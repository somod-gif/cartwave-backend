package com.cartwave.cart.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartDTO {

    private UUID id;
    private UUID storeId;
    private UUID customerId;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String currency;
    private List<CartItemDTO> items;
}
