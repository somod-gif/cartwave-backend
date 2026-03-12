package com.cartwave.store.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.product.dto.ProductDTO;
import com.cartwave.product.service.ProductService;
import com.cartwave.store.dto.StoreBrandingRequest;
import com.cartwave.store.dto.StoreCreateRequest;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.dto.StoreDomainRequest;
import com.cartwave.store.dto.StoreSeoRequest;
import com.cartwave.store.dto.StoreUpdateRequest;
import com.cartwave.store.service.StoreService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Stores", description = "Store management endpoints")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final ProductService productService;

    @Operation(summary = "List accessible stores")
    @GetMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<StoreDTO>>> listStores() {
        return ResponseEntity.ok(ApiResponse.success("Stores retrieved successfully", storeService.listStores()));
    }

    @Operation(summary = "Create a new store")
    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreDTO>> createStore(@Valid @RequestBody StoreCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Store created successfully", storeService.createStore(request)));
    }

    @Operation(summary = "Get store by ID (authenticated)")
    @GetMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<StoreDTO>> getStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success("Store retrieved successfully", storeService.getStoreById(storeId)));
    }

    @Operation(summary = "Update store details")
    @PutMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreDTO>> updateStore(
            @PathVariable UUID storeId,
            @RequestBody StoreUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Store updated successfully", storeService.updateStore(storeId, request)));
    }

    @Operation(summary = "Delete (soft-delete) a store")
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable UUID storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.ok(ApiResponse.success("Store deleted successfully", null));
    }

    // ── V2 Store Builder endpoints ─────────────────────────────────────────────

    @Operation(summary = "Update store branding (logo, banner, brand colour, template)")
    @PutMapping("/{id}/branding")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreDTO>> updateBranding(
            @PathVariable UUID id,
            @RequestBody StoreBrandingRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Store branding updated", storeService.updateBranding(id, request)));
    }

    @Operation(summary = "Set store custom domain")
    @PutMapping("/{id}/domain")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreDTO>> updateDomain(
            @PathVariable UUID id,
            @RequestBody StoreDomainRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Store domain updated", storeService.updateDomain(id, request)));
    }

    @Operation(summary = "Update store SEO metadata")
    @PutMapping("/{id}/seo")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreDTO>> updateSeo(
            @PathVariable UUID id,
            @RequestBody StoreSeoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Store SEO updated", storeService.updateSeo(id, request)));
    }

    @Operation(summary = "Public storefront view — no auth required")
    @GetMapping("/{id}/public")
    public ResponseEntity<ApiResponse<StoreDTO>> publicStore(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Store retrieved", storeService.getPublicStoreById(id)));
    }

    @Operation(summary = "Public published product listing for a store — no auth required")
    @GetMapping("/{storeId}/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getStoreProducts(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", productService.getPublishedProductsByStoreId(storeId)));
    }
}
