package com.cartwave.cart.controller;

import com.cartwave.cart.dto.CartDTO;
import com.cartwave.cart.dto.CartItemRequest;
import com.cartwave.cart.service.CartService;
import com.cartwave.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart() {
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cartService.getCurrentCart()));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<CartDTO>> getItems() {
        return ResponseEntity.ok(ApiResponse.success("Cart items retrieved successfully", cartService.getCurrentCart()));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cart item added successfully", cartService.addItem(request)));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cartService.updateItem(itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(@PathVariable UUID itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Cart item removed successfully", null));
    }
}
