package com.cartwave.checkout.service;

import com.cartwave.billing.entity.BillingStatus;
import com.cartwave.billing.entity.BillingTransaction;
import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.cart.entity.Cart;
import com.cartwave.cart.entity.CartItem;
import com.cartwave.cart.service.CartService;
import com.cartwave.checkout.dto.CheckoutRequest;
import com.cartwave.checkout.dto.CheckoutResponse;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.service.CustomerService;
import com.cartwave.exception.BusinessException;
import com.cartwave.order.entity.Order;
import com.cartwave.order.entity.OrderStatus;
import com.cartwave.order.entity.PaymentStatus;
import com.cartwave.order.entity.OrderItem;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.order.repository.OrderItemRepository;
import com.cartwave.product.entity.Product;
import com.cartwave.product.entity.ProductStatus;
import com.cartwave.product.repository.ProductRepository;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutService {

    private final CartService cartService;
    private final CustomerService customerService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final BillingTransactionRepository billingTransactionRepository;

    public CheckoutResponse checkout(CheckoutRequest request) {
        Customer customer = customerService.requireCurrentCustomer();
        Cart cart = cartService.getOrCreateActiveCart();
        List<CartItem> items = cartService.getActiveItems(cart);
        if (items.isEmpty()) {
            throw new BusinessException("EMPTY_CART", "Cart is empty.");
        }

        UUID storeId = TenantContext.getTenantId();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            Product product = productRepository.findByIdAndStoreId(item.getProductId(), storeId)
                    .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "A cart item no longer exists."));
            if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_STOCK", "Insufficient stock for product " + product.getName());
            }
            total = total.add(item.getLineTotal());
        }

        Order order = Order.builder()
                .storeId(storeId)
                .customerId(customer.getId())
                .orderNumber(generateOrderNumber())
                .totalAmount(total)
                .shippingCost(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .deliveryAddress(request.getDeliveryAddress())
                .customerEmail(request.getCustomerEmail())
                .customerPhoneNumber(request.getCustomerPhoneNumber())
                .notes(request.getNotes())
                .build();
        Order savedOrder = orderRepository.save(order);

        for (CartItem item : items) {
            Product product = productRepository.findByIdAndStoreId(item.getProductId(), storeId)
                    .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "A cart item no longer exists."));
            product.setStock(product.getStock() - item.getQuantity());
            if (product.getStock() <= 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(product.getId())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .lineTotal(item.getLineTotal())
                    .build();
            orderItemRepository.save(orderItem);
        }

        BillingTransaction transaction = BillingTransaction.builder()
                .storeId(storeId)
                .orderId(savedOrder.getId())
                .transactionId(generateTransactionId())
                .amount(total)
                .currency(cart.getCurrency())
                .status(BillingStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentProvider(request.getPaymentProvider())
                .transactionDetails("checkout-created")
                .build();
        BillingTransaction savedTransaction = billingTransactionRepository.save(transaction);

        cartService.markCheckedOut(cart);

        return CheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .billingTransactionId(savedTransaction.getId())
                .transactionId(savedTransaction.getTransactionId())
                .totalAmount(savedOrder.getTotalAmount())
                .orderStatus(savedOrder.getStatus().name())
                .paymentStatus(savedOrder.getPaymentStatus().name())
                .build();
    }

    private String generateOrderNumber() {
        return "CW-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private String generateTransactionId() {
        return "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
