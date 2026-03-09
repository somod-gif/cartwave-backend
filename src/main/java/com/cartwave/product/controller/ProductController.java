package com.cartwave.product.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.product.dto.ProductDTO;
import com.cartwave.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Product catalogue management")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a product (enforces subscription limit)")
    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", productService.createProduct(productDto)));
    }

    @Operation(summary = "List all products in the current store")
    @GetMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", productService.getAllProducts()));
    }

    @Operation(summary = "Get a product by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", productService.getProductById(id)));
    }

    @Operation(summary = "Update a product")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductDTO productDto) {
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", productService.updateProduct(id, productDto)));
    }

    @Operation(summary = "Delete a product (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    // ── V2 endpoints ──────────────────────────────────────────────────────────

    @Operation(summary = "Upload images to S3 and attach to product")
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductDTO>> uploadImages(
            @PathVariable UUID id,
            @RequestParam("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(ApiResponse.success("Images uploaded", productService.uploadImages(id, files)));
    }

    @Operation(summary = "Remove a specific image from a product and S3")
    @DeleteMapping("/{id}/images/{imageUrl}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductDTO>> removeImage(
            @PathVariable UUID id,
            @PathVariable String imageUrl
    ) {
        return ResponseEntity.ok(ApiResponse.success("Image removed", productService.removeImage(id, imageUrl)));
    }

    @Operation(summary = "Toggle product publish status")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductDTO>> togglePublish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Product publish status toggled", productService.togglePublish(id)));
    }
}
