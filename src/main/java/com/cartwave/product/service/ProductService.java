package com.cartwave.product.service;

import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.product.dto.ProductDTO;
import com.cartwave.product.entity.Product;
import com.cartwave.product.entity.ProductStatus;
import com.cartwave.product.repository.ProductRepository;
import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.subscription.service.SubscriptionService;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final SubscriptionService subscriptionService;

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
        return toDto(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        UUID storeId = TenantContext.getTenantId();
        return productRepository.findAllByStoreId(storeId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDto(product);
    }

    public ProductDTO updateProduct(UUID id, ProductDTO productDto) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        applyProduct(product, productDto, false);
        return toDto(productRepository.save(product));
    }

    public void deleteProduct(UUID id) {
        UUID storeId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getPublicProducts(String slug) {
        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "slug", slug));
        return productRepository.findAllByStoreId(store.getId()).stream()
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                .map(this::toDto)
                .toList();
    }

    private void applyProduct(Product product, ProductDTO dto, boolean creating) {
        if (dto.getName() != null) {
            product.setName(dto.getName());
        } else if (creating) {
            throw new BusinessException("PRODUCT_NAME_REQUIRED", "Product name is required.");
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        } else if (creating) {
            throw new BusinessException("PRODUCT_PRICE_REQUIRED", "Product price is required.");
        }
        if (dto.getCostPrice() != null) {
            product.setCostPrice(dto.getCostPrice());
        }
        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }
        if (dto.getLowStockThreshold() != null) {
            product.setLowStockThreshold(dto.getLowStockThreshold());
        }
        if (dto.getSku() != null) {
            product.setSku(dto.getSku());
        }
        if (dto.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(dto.getStatus()));
        }
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
        if (dto.getImages() != null) {
            product.setImages(dto.getImages());
        }
        if (dto.getCategory() != null) {
            product.setCategory(dto.getCategory());
        }
        if (dto.getAttributes() != null) {
            product.setAttributes(dto.getAttributes());
        }
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
        return dto;
    }
}
