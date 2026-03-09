package com.cartwave.marketing.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_store_id", columnList = "store_id"),
        @Index(name = "idx_coupon_code", columnList = "code"),
        @Index(name = "idx_coupon_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Coupon extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    /** Unique coupon code, e.g. SAVE20 */
    @Column(nullable = false, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    /** Discount value — percentage (0-100) or fixed amount in Naira */
    @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    /** Minimum order value required to use the coupon */
    @Column(name = "min_order_value", precision = 19, scale = 2)
    private BigDecimal minOrderValue;

    /** Maximum number of uses allowed (null = unlimited) */
    @Column(name = "max_uses")
    private Integer maxUses;

    /** Current number of times this coupon has been used */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /** Coupon expiry timestamp */
    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
