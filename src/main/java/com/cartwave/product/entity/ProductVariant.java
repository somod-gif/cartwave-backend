package com.cartwave.product.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variants", uniqueConstraints = {
        @UniqueConstraint(name = "uq_variant_sku", columnNames = "sku")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductVariant extends BaseEntity {

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 100)
    private String variantName;

    @Column(length = 100)
    private String sku;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false)
    private Long stockQuantity = 0L;

    @Column(length = 500)
    private String imageUrl;
}
