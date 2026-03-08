package com.cartwave.store.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.product.dto.ProductDTO;
import com.cartwave.product.service.ProductService;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
