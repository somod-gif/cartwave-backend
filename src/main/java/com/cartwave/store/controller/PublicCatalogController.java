package com.cartwave.store.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.product.dto.ProductDTO;
import com.cartwave.product.service.ProductService;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/public/stores")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final StoreService storeService;
    private final ProductService productService;

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<StoreDTO>> getStore(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Store retrieved successfully", storeService.getPublicStoreBySlug(slug)));
    }

    @GetMapping("/{slug}/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProducts(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", productService.getPublicProducts(slug)));
    }

    // ── Public product search (no auth required) ──────────────────────────────

    @GetMapping("/{storeId}/products/search")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProductDTO> results = productService.searchProducts(
                storeId, q, category, minPrice, maxPrice, inStock, true, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }
}

