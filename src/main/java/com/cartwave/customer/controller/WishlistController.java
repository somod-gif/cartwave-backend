package com.cartwave.customer.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.customer.dto.WishlistItemDTO;
import com.cartwave.customer.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Wishlist", description = "Customer wishlist management")
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Get current customer wishlist")
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<WishlistItemDTO>>> getWishlist() {
        return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved", wishlistService.getWishlist()));
    }

    @Operation(summary = "Add a product to wishlist")
    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<WishlistItemDTO>> addToWishlist(@PathVariable UUID productId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Added to wishlist", wishlistService.addToWishlist(productId)));
    }

    @Operation(summary = "Remove a product from wishlist")
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(@PathVariable UUID productId) {
        wishlistService.removeFromWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }
}
