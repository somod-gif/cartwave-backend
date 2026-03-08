package com.cartwave.cart.service;

import com.cartwave.cart.dto.CartDTO;
import com.cartwave.cart.dto.CartItemDTO;
import com.cartwave.cart.dto.CartItemRequest;
import com.cartwave.cart.entity.Cart;
import com.cartwave.cart.entity.CartItem;
import com.cartwave.cart.entity.CartStatus;
import com.cartwave.cart.repository.CartItemRepository;
import com.cartwave.cart.repository.CartRepository;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.service.CustomerService;
import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.product.entity.Product;
import com.cartwave.product.entity.ProductStatus;
import com.cartwave.product.repository.ProductRepository;
import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CustomerService customerService;

    @Transactional(readOnly = true)
    public CartDTO getCurrentCart() {
        return toDto(getOrCreateActiveCart(), cartItemRepository.findByCartId(getOrCreateActiveCart().getId()));
    }

    public CartDTO addItem(CartItemRequest request) {
        Cart cart = getOrCreateActiveCart();
        Product product = getActiveProduct(request.getProductId(), cart.getStoreId());

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> CartItem.builder()
                        .cartId(cart.getId())
                        .productId(product.getId())
                        .quantity(0)
                        .unitPrice(product.getPrice())
                        .lineTotal(BigDecimal.ZERO)
                        .build());

        item.setQuantity(item.getQuantity() + request.getQuantity());
        item.setUnitPrice(product.getPrice());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        cartItemRepository.save(item);
        recalculate(cart);
        return getCurrentCart();
    }

    public CartDTO updateItem(UUID itemId, CartItemRequest request) {
        Cart cart = getOrCreateActiveCart();
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
        Product product = getActiveProduct(item.getProductId(), cart.getStoreId());

        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        cartItemRepository.save(item);
        recalculate(cart);
        return getCurrentCart();
    }

    public void removeItem(UUID itemId) {
        Cart cart = getOrCreateActiveCart();
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
        item.setDeleted(true);
        cartItemRepository.save(item);
        recalculate(cart);
    }

    public void markCheckedOut(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        items.forEach(item -> item.setDeleted(true));
        cartItemRepository.saveAll(items);
        cart.setStatus(CartStatus.CHECKED_OUT);
        cart.setSubtotal(BigDecimal.ZERO);
        cart.setTotal(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getActiveItems(Cart cart) {
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Transactional(readOnly = true)
    public Cart getOrCreateActiveCart() {
        Customer customer = customerService.requireCurrentCustomer();
        UUID storeId = TenantContext.getTenantId();
        return cartRepository.findByCustomerIdAndStoreIdAndStatus(customer.getId(), storeId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Store store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
                    Cart cart = Cart.builder()
                            .customerId(customer.getId())
                            .storeId(storeId)
                            .status(CartStatus.ACTIVE)
                            .subtotal(BigDecimal.ZERO)
                            .total(BigDecimal.ZERO)
                            .currency(store.getCurrency() == null ? "USD" : store.getCurrency())
                            .build();
                    return cartRepository.save(cart);
                });
    }

    private void recalculate(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        BigDecimal subtotal = items.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setSubtotal(subtotal);
        cart.setTotal(subtotal);
        cartRepository.save(cart);
    }

    private Product getActiveProduct(UUID productId, UUID storeId) {
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        if (product.getStatus() != ProductStatus.ACTIVE || Boolean.TRUE.equals(product.getDeleted())) {
            throw new BusinessException("PRODUCT_UNAVAILABLE", "Product is not available for cart operations.");
        }
        return product;
    }

    private CartDTO toDto(Cart cart, List<CartItem> items) {
        return CartDTO.builder()
                .id(cart.getId())
                .storeId(cart.getStoreId())
                .customerId(cart.getCustomerId())
                .status(cart.getStatus().name())
                .subtotal(cart.getSubtotal())
                .total(cart.getTotal())
                .currency(cart.getCurrency())
                .items(items.stream().map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    return CartItemDTO.builder()
                            .id(item.getId())
                            .productId(item.getProductId())
                            .productName(product == null ? null : product.getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .lineTotal(item.getLineTotal())
                            .build();
                }).toList())
                .build();
    }
}
