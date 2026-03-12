package com.cartwave.product.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.service.CustomerService;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.order.repository.OrderItemRepository;
import com.cartwave.config.AwsS3Service;
import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.product.dto.ProductDTO;
import com.cartwave.product.dto.ProductVariantDTO;
import com.cartwave.product.dto.ReviewDTO;
import com.cartwave.product.entity.Product;
import com.cartwave.product.entity.ProductStatus;
import com.cartwave.product.entity.ProductVariant;
import com.cartwave.product.entity.Review;
import com.cartwave.product.repository.ProductRepository;
import com.cartwave.product.repository.ProductVariantRepository;
import com.cartwave.product.repository.ReviewRepository;
import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.subscription.service.SubscriptionService;
import com.cartwave.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final SubscriptionService subscriptionService;
    private final AwsS3Service awsS3Service;
    private final CustomerService customerService;
    private final OrderItemRepository orderItemRepository;

    @CacheEvict(value = "store-products", allEntries = true)
    public ProductDTO createProduct(ProductDTO productDto) {
        UUID storeId = TenantContext.getTenantId();
        long currentCount = productRepository.countByStoreIdAndDeletedFalse(storeId);
        subscriptionService.assertCanCreateProducts(storeId, currentCount, 1);

        Product product = new Product();
        applyProduct(product, productDto, true);
        product.setStoreId(storeId);
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        if (product.getStock() == null) {
            product.setStock(0L);
        }
        if (product.getIsPublished() == null) {
            product.setIsPublished(false);
        }
        return toDto(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(int page, int size) {
        UUID storeId = TenantContext.getTenantId();
        return productRepository.findByStoreId(storeId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDto(product);
    }

    @CacheEvict(value = "store-products", allEntries = true)
    public ProductDTO updateProduct(UUID id, ProductDTO productDto) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        applyProduct(product, productDto, false);
        return toDto(productRepository.save(product));
    }

    @CacheEvict(value = "store-products", allEntries = true)
    public void deleteProduct(UUID id) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "store-products", key = "#slug")
    public List<ProductDTO> getPublicProducts(String slug) {
        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "slug", slug));
        return productRepository.findAllByStoreId(store.getId()).stream()
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getPublishedProductsByStoreId(UUID storeId) {
        return productRepository.findAllByStoreId(storeId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsPublished()) && p.getStatus() == ProductStatus.ACTIVE)
                .map(this::toDto)
                .toList();
    }

    /** Upload images to S3 and append their URLs to the product's images field. */
    public ProductDTO uploadImages(UUID productId, List<MultipartFile> files) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<String> urls = new ArrayList<>(existingImages(product));
        for (MultipartFile file : files) {
            String url = awsS3Service.upload("products/" + storeId, file);
            urls.add(url);
        }
        product.setImages(String.join(",", urls));
        if (product.getImageUrl() == null && !urls.isEmpty()) {
            product.setImageUrl(urls.get(0));
        }
        return toDto(productRepository.save(product));
    }

    /** Remove a single image URL from the product and delete from S3. */
    public ProductDTO removeImage(UUID productId, String imageUrl) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<String> urls = new ArrayList<>(existingImages(product));
        if (!urls.remove(imageUrl)) {
            throw new BusinessException("IMAGE_NOT_FOUND", "Image URL not found on this product.");
        }
        awsS3Service.delete(imageUrl);
        product.setImages(String.join(",", urls));
        if (imageUrl.equals(product.getImageUrl())) {
            product.setImageUrl(urls.isEmpty() ? null : urls.get(0));
        }
        return toDto(productRepository.save(product));
    }

    /** Toggle the isPublished flag. */
    @CacheEvict(value = "store-products", allEntries = true)
    public ProductDTO togglePublish(UUID productId) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setIsPublished(!Boolean.TRUE.equals(product.getIsPublished()));
        return toDto(productRepository.save(product));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private List<String> existingImages(Product product) {
        if (product.getImages() == null || product.getImages().isBlank()) return new ArrayList<>();
        return Arrays.stream(product.getImages().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void applyProduct(Product product, ProductDTO dto, boolean creating) {
        if (dto.getName() != null) {
            product.setName(dto.getName());
        } else if (creating) {
            throw new BusinessException("PRODUCT_NAME_REQUIRED", "Product name is required.");
        }
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        } else if (creating) {
            throw new BusinessException("PRODUCT_PRICE_REQUIRED", "Product price is required.");
        }
        if (dto.getCostPrice() != null)        product.setCostPrice(dto.getCostPrice());
        if (dto.getStock() != null)            product.setStock(dto.getStock());
        if (dto.getLowStockThreshold() != null) product.setLowStockThreshold(dto.getLowStockThreshold());
        if (dto.getSku() != null)              product.setSku(dto.getSku());
        if (dto.getStatus() != null)           product.setStatus(ProductStatus.valueOf(dto.getStatus()));
        if (dto.getImageUrl() != null)         product.setImageUrl(dto.getImageUrl());
        if (dto.getImages() != null)           product.setImages(dto.getImages());
        if (dto.getCategory() != null)         product.setCategory(dto.getCategory());
        if (dto.getAttributes() != null)       product.setAttributes(dto.getAttributes());
        // V2 fields
        if (dto.getTags() != null)             product.setTags(dto.getTags());
        if (dto.getIsPublished() != null)      product.setIsPublished(dto.getIsPublished());
        if (dto.getSeoTitle() != null)         product.setSeoTitle(dto.getSeoTitle());
        if (dto.getSeoDescription() != null)   product.setSeoDescription(dto.getSeoDescription());
    }

    public ProductDTO toDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setStoreId(product.getStoreId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setStock(product.getStock());
        dto.setLowStockThreshold(product.getLowStockThreshold());
        dto.setSku(product.getSku());
        dto.setStatus(product.getStatus() == null ? null : product.getStatus().name());
        dto.setImageUrl(product.getImageUrl());
        dto.setImages(product.getImages());
        dto.setCategory(product.getCategory());
        dto.setAttributes(product.getAttributes());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        // V2 fields
        dto.setTags(product.getTags());
        dto.setIsPublished(product.getIsPublished());
        dto.setSeoTitle(product.getSeoTitle());
        dto.setSeoDescription(product.getSeoDescription());
        return dto;
    }

    // ── Search / Filter ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(
            UUID storeId,
            String q,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            boolean inStockOnly,
            boolean publishedOnly,
            int page,
            int size) {
        // If no storeId provided, resolve from tenant context (authenticated endpoints)
        UUID resolvedStoreId = storeId != null ? storeId : TenantContext.getTenantId();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return productRepository.search(resolvedStoreId, q, category, minPrice, maxPrice, inStockOnly, publishedOnly, pageable)
                .map(this::toDto);
    }

    // ── Product Variants ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductVariantDTO> getVariants(UUID productId) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return productVariantRepository.findByProductIdAndDeletedFalse(product.getId())
                .stream().map(this::toVariantDto).toList();
    }

    public ProductVariantDTO addVariant(UUID productId, ProductVariantDTO dto) {
        UUID storeId = TenantContext.getTenantId();
        productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        ProductVariant variant = ProductVariant.builder()
                .productId(productId)
                .variantName(dto.getVariantName())
                .sku(dto.getSku())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0L)
                .imageUrl(dto.getImageUrl())
                .build();
        return toVariantDto(productVariantRepository.save(variant));
    }

    public ProductVariantDTO updateVariant(UUID productId, UUID variantId, ProductVariantDTO dto) {
        UUID storeId = TenantContext.getTenantId();
        productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        ProductVariant variant = productVariantRepository.findByIdAndDeletedFalse(variantId)
                .filter(v -> v.getProductId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));
        if (dto.getVariantName() != null) variant.setVariantName(dto.getVariantName());
        if (dto.getSku() != null) variant.setSku(dto.getSku());
        if (dto.getPrice() != null) variant.setPrice(dto.getPrice());
        if (dto.getStockQuantity() != null) variant.setStockQuantity(dto.getStockQuantity());
        if (dto.getImageUrl() != null) variant.setImageUrl(dto.getImageUrl());
        return toVariantDto(productVariantRepository.save(variant));
    }

    public void deleteVariant(UUID productId, UUID variantId) {
        UUID storeId = TenantContext.getTenantId();
        productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        ProductVariant variant = productVariantRepository.findByIdAndDeletedFalse(variantId)
                .filter(v -> v.getProductId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));
        variant.setDeleted(true);
        productVariantRepository.save(variant);
    }

    private ProductVariantDTO toVariantDto(ProductVariant v) {
        return ProductVariantDTO.builder()
                .id(v.getId())
                .productId(v.getProductId())
                .variantName(v.getVariantName())
                .sku(v.getSku())
                .price(v.getPrice())
                .stockQuantity(v.getStockQuantity())
                .imageUrl(v.getImageUrl())
                .createdAt(v.getCreatedAt())
                .build();
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviews(UUID productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return reviewRepository.findByProductIdAndDeletedFalse(productId, pageable)
                .map(this::toReviewDto);
    }

    public ReviewDTO addReview(UUID productId, ReviewDTO dto) {
        UUID storeId = TenantContext.getTenantId();
        productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Customer customer = customerService.requireCurrentCustomer();
        if (reviewRepository.existsByProductIdAndCustomerIdAndDeletedFalse(productId, customer.getId())) {
            throw new BusinessException("REVIEW_EXISTS", "You have already reviewed this product.");
        }

        boolean verified = orderItemRepository.existsPurchaseByCustomer(productId, customer.getId());

        Review review = Review.builder()
                .productId(productId)
                .customerId(customer.getId())
                .storeId(storeId)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .verified(verified)
                .build();
        return toReviewDto(reviewRepository.save(review));
    }

    public void deleteReview(UUID productId, UUID reviewId) {
        UUID storeId = TenantContext.getTenantId();
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> r.getProductId().equals(productId) && r.getStoreId().equals(storeId) && !r.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        review.setDeleted(true);
        reviewRepository.save(review);
    }

    private ReviewDTO toReviewDto(Review r) {
        return ReviewDTO.builder()
                .id(r.getId())
                .productId(r.getProductId())
                .customerId(r.getCustomerId())
                .storeId(r.getStoreId())
                .rating(r.getRating())
                .comment(r.getComment())
                .verified(r.getVerified())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
