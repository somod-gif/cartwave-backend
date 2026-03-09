package com.cartwave.marketing.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.marketing.dto.CouponRequest;
import com.cartwave.marketing.dto.CouponResponse;
import com.cartwave.marketing.dto.CouponValidateRequest;
import com.cartwave.marketing.dto.CouponValidateResponse;
import com.cartwave.marketing.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marketing")
@RequiredArgsConstructor
@Tag(name = "Marketing", description = "Coupon and discount management for stores")
public class MarketingController {

    private final CouponService couponService;

    @PostMapping("/stores/{storeId}/coupons")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create a new coupon for a store")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @PathVariable UUID storeId,
            @Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Coupon created", response));
    }

    @GetMapping("/stores/{storeId}/coupons")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN','SUPER_ADMIN')")
    @Operation(summary = "List all coupons for a store")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getCoupons(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupons(storeId)));
    }

    @DeleteMapping("/stores/{storeId}/coupons/{couponId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete a coupon")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(
            @PathVariable UUID storeId,
            @PathVariable UUID couponId) {
        couponService.deleteCoupon(storeId, couponId);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted", null));
    }

    /** Public endpoint — called at checkout to apply a coupon */
    @PostMapping("/coupons/validate")
    @Operation(summary = "Validate a coupon code (public)", description = "Validates a coupon and returns the discount amount. Does not increment usage count.")
    public ResponseEntity<ApiResponse<CouponValidateResponse>> validateCoupon(
            @Valid @RequestBody CouponValidateRequest request) {
        CouponValidateResponse response = couponService.validateCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
