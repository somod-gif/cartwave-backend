package com.cartwave.customer.service;

import com.cartwave.customer.dto.WishlistItemDTO;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.entity.Wishlist;
import com.cartwave.customer.repository.WishlistRepository;
import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CustomerService customerService;

    @Transactional(readOnly = true)
    public List<WishlistItemDTO> getWishlist() {
        UUID storeId = TenantContext.getTenantId();
        Customer customer = customerService.requireCurrentCustomer();
        return wishlistRepository.findByCustomerIdAndStoreIdAndDeletedFalse(customer.getId(), storeId)
                .stream().map(this::toDto).toList();
    }

    public WishlistItemDTO addToWishlist(UUID productId) {
        UUID storeId = TenantContext.getTenantId();
        Customer customer = customerService.requireCurrentCustomer();

        if (wishlistRepository.existsByCustomerIdAndProductIdAndDeletedFalse(customer.getId(), productId)) {
            throw new BusinessException("WISHLIST_EXISTS", "Product is already in your wishlist.");
        }

        Wishlist item = Wishlist.builder()
                .customerId(customer.getId())
                .productId(productId)
                .storeId(storeId)
                .savedAt(Instant.now())
                .build();
        return toDto(wishlistRepository.save(item));
    }

    public void removeFromWishlist(UUID productId) {
        Customer customer = customerService.requireCurrentCustomer();
        Wishlist item = wishlistRepository.findByCustomerIdAndProductIdAndDeletedFalse(customer.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("WishlistItem", "productId", productId));
        item.setDeleted(true);
        wishlistRepository.save(item);
    }

    private WishlistItemDTO toDto(Wishlist w) {
        return WishlistItemDTO.builder()
                .id(w.getId())
                .productId(w.getProductId())
                .storeId(w.getStoreId())
                .savedAt(w.getSavedAt())
                .build();
    }
}
