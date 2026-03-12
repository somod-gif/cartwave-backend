package com.cartwave.product.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.product.dto.ProductDTO;
import com.cartwave.product.dto.ProductVariantDTO;
import com.cartwave.product.dto.ReviewDTO;
import com.cartwave.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", productService.getAllProducts(page, size)));
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

    // ── Search / filter (store-scoped, authenticated) ─────────────────────────

    @Operation(summary = "Search products with filters")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "false") boolean publishedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProductDTO> results = productService.searchProducts(
                null, q, category, minPrice, maxPrice, inStock, publishedOnly, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    // ── Variants ──────────────────────────────────────────────────────────────

    @Operation(summary = "List variants for a product")
    @GetMapping("/{id}/variants")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<ProductVariantDTO>>> getVariants(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Variants retrieved", productService.getVariants(id)));
    }

    @Operation(summary = "Add a variant to a product")
    @PostMapping("/{id}/variants")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> addVariant(
            @PathVariable UUID id,
            @Valid @RequestBody ProductVariantDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Variant added", productService.addVariant(id, dto)));
    }

    @Operation(summary = "Update a product variant")
    @PutMapping("/{id}/variants/{variantId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> updateVariant(
            @PathVariable UUID id,
            @PathVariable UUID variantId,
            @Valid @RequestBody ProductVariantDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Variant updated", productService.updateVariant(id, variantId, dto)));
    }

    @Operation(summary = "Delete a product variant")
    @DeleteMapping("/{id}/variants/{variantId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable UUID id,
            @PathVariable UUID variantId) {
        productService.deleteVariant(id, variantId);
        return ResponseEntity.ok(ApiResponse.success("Variant deleted", null));
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    @Operation(summary = "List reviews for a product (paginated)")
    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getReviews(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", productService.getReviews(id, page, size)));
    }

    @Operation(summary = "Submit a review (customers only, verified-purchase flag auto-set)")
    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted", productService.addReview(id, dto)));
    }

    @Operation(summary = "Delete a review (admins / business owner)")
    @DeleteMapping("/{id}/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID id,
            @PathVariable UUID reviewId) {
        productService.deleteReview(id, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
