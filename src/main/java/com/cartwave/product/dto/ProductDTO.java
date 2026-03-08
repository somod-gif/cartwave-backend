package com.cartwave.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductDTO {
    private UUID id;
    private UUID storeId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Long stock;
    private Long lowStockThreshold;
    private String sku;
    private String status;
    private String imageUrl;
    private String images;
    private String category;
    private String attributes;
}
