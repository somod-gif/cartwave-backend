package com.cartwave.marketing.service;

import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.marketing.dto.CouponRequest;
import com.cartwave.marketing.dto.CouponResponse;
import com.cartwave.marketing.dto.CouponValidateRequest;
import com.cartwave.marketing.dto.CouponValidateResponse;
import com.cartwave.marketing.entity.Coupon;
import com.cartwave.marketing.entity.DiscountType;
import com.cartwave.marketing.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponResponse createCoupon(UUID storeId, CouponRequest request) {
        if (couponRepository.existsByStoreIdAndCodeIgnoreCaseAndDeletedFalse(storeId, request.getCode())) {
            throw new BusinessException("COUPON_CODE_EXISTS", "A coupon with this code already exists for this store.");
        }
        Coupon coupon = Coupon.builder()
                .storeId(storeId)
                .code(request.getCode().trim().toUpperCase())
                .discountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()))
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .maxUses(request.getMaxUses())
                .expiresAt(request.getExpiresAt())
                .active(true)
                .usedCount(0)
                .build();
        return toDto(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getCoupons(UUID storeId) {
        return couponRepository.findAllByStoreIdAndDeletedFalse(storeId).stream().map(this::toDto).toList();
    }

    public void deleteCoupon(UUID storeId, UUID couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", couponId));
        if (!coupon.getStoreId().equals(storeId)) {
            throw new BusinessException("ACCESS_DENIED", "You do not own this coupon.");
        }
        coupon.setDeleted(true);
        couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public CouponValidateResponse validateCoupon(CouponValidateRequest request) {
        Coupon coupon = couponRepository
                .findByStoreIdAndCodeIgnoreCaseAndDeletedFalse(request.getStoreId(), request.getCode())
                .orElse(null);

        if (coupon == null || !coupon.getActive()) {
            return CouponValidateResponse.builder().valid(false).code(request.getCode()).message("Coupon not found or inactive.").build();
        }
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(Instant.now())) {
            return CouponValidateResponse.builder().valid(false).code(request.getCode()).message("Coupon has expired.").build();
        }
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            return CouponValidateResponse.builder().valid(false).code(request.getCode()).message("Coupon usage limit reached.").build();
        }
        if (coupon.getMinOrderValue() != null && request.getOrderAmount().compareTo(coupon.getMinOrderValue()) < 0) {
            return CouponValidateResponse.builder().valid(false).code(request.getCode())
                    .message(String.format("Minimum order value of ₦%.2f required.", coupon.getMinOrderValue())).build();
        }

        BigDecimal discountAmount = computeDiscount(coupon.getDiscountType(), coupon.getDiscountValue(), request.getOrderAmount());
        BigDecimal finalAmount = request.getOrderAmount().subtract(discountAmount).max(BigDecimal.ZERO);

        return CouponValidateResponse.builder()
                .valid(true)
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType().name())
                .discountValue(coupon.getDiscountValue())
                .discountAmount(discountAmount)
                .finalOrderAmount(finalAmount)
                .message("Coupon applied successfully.")
                .build();
    }

    /** Increment usedCount when coupon is applied at checkout */
    public void applyCoupon(UUID storeId, String code) {
        couponRepository.findByStoreIdAndCodeIgnoreCaseAndDeletedFalse(storeId, code).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        });
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BigDecimal computeDiscount(DiscountType type, BigDecimal value, BigDecimal orderAmount) {
        return switch (type) {
            case PERCENT -> orderAmount.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED -> value.min(orderAmount);
        };
    }

    private CouponResponse toDto(Coupon c) {
        return CouponResponse.builder()
                .id(c.getId())
                .storeId(c.getStoreId())
                .code(c.getCode())
                .discountType(c.getDiscountType().name())
                .discountValue(c.getDiscountValue())
                .minOrderValue(c.getMinOrderValue())
                .maxUses(c.getMaxUses())
                .usedCount(c.getUsedCount())
                .expiresAt(c.getExpiresAt())
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
