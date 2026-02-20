package com.cartwave.product.service;

import com.cartwave.product.entity.Product;
import com.cartwave.product.entity.ProductStatus;
import com.cartwave.product.repository.ProductRepository;
import com.cartwave.product.dto.ProductDTO;
import com.cartwave.subscription.service.SubscriptionService;
import com.cartwave.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SubscriptionService subscriptionService;

    public ProductService(ProductRepository productRepository, SubscriptionService subscriptionService) {
        this.productRepository = productRepository;
        this.subscriptionService = subscriptionService;
    }

    public ProductDTO createProduct(ProductDTO productDto) {
        UUID storeId = TenantContext.getTenantId();
        long currentCount = productRepository.countByStoreIdAndDeletedFalse(storeId);
        subscriptionService.assertCanCreateProducts(storeId, currentCount, 1);

        Product product = new Product();
        product.setStoreId(storeId);
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStatus(ProductStatus.ACTIVE);
        product.setDeleted(false);
        product.setStock(0L);
        Product savedProduct = productRepository.save(product);
        return toDto(savedProduct);
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProductDTO getProductById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return toDto(product);
    }

    public ProductDTO updateProduct(UUID id, ProductDTO productDto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        Product updatedProduct = productRepository.save(product);
        return toDto(updatedProduct);
    }

    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }

    private ProductDTO toDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        return dto;
    }
}
