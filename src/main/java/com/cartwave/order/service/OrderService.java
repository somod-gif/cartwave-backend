package com.cartwave.order.service;

import com.cartwave.billing.entity.BillingStatus;
import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.service.CustomerService;
import com.cartwave.escrow.service.EscrowService;
import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.order.dto.OrderDTO;
import com.cartwave.order.entity.Order;
import com.cartwave.order.entity.OrderStatus;
import com.cartwave.order.entity.PaymentStatus;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.security.model.CurrentUserPrincipal;
import com.cartwave.security.service.CurrentUserService;
import com.cartwave.tenant.TenantContext;
import com.cartwave.user.entity.UserRole;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;
    private final CustomerService customerService;
    private final BillingTransactionRepository billingTransactionRepository;
    private final EscrowService escrowService;

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(UUID orderId) {
        UUID storeId = TenantContext.getTenantId();
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        ensureOrderAccess(order);
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStore() {
        UUID storeId = TenantContext.getTenantId();
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        if (principal.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerService.requireCurrentCustomer();
            return orderRepository.findByCustomerIdAndStoreId(customer.getId(), storeId).stream().map(this::toDto).toList();
        }
        return orderRepository.findByStoreId(storeId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public OrderDTO createOrder(OrderDTO orderDTO) {
        UUID storeId = TenantContext.getTenantId();
        Order order = Order.builder()
                .storeId(storeId)
                .customerId(orderDTO.getCustomerId())
                .orderNumber(orderDTO.getOrderNumber() == null ? generateOrderNumber() : orderDTO.getOrderNumber())
                .totalAmount(orderDTO.getTotalAmount() == null ? BigDecimal.ZERO : orderDTO.getTotalAmount())
                .shippingCost(defaultAmount(orderDTO.getShippingCost()))
                .taxAmount(defaultAmount(orderDTO.getTaxAmount()))
                .discountAmount(defaultAmount(orderDTO.getDiscountAmount()))
                .status(orderDTO.getStatus() == null ? OrderStatus.PENDING : OrderStatus.valueOf(orderDTO.getStatus()))
                .paymentStatus(orderDTO.getPaymentStatus() == null ? PaymentStatus.PENDING : PaymentStatus.valueOf(orderDTO.getPaymentStatus()))
                .deliveryAddress(orderDTO.getDeliveryAddress())
                .customerEmail(orderDTO.getCustomerEmail())
                .customerPhoneNumber(orderDTO.getCustomerPhoneNumber())
                .notes(orderDTO.getNotes())
                .build();

        return toDto(orderRepository.save(order));
    }

    public OrderDTO updateOrder(UUID orderId, OrderDTO orderDTO) {
        UUID storeId = TenantContext.getTenantId();
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        ensureNonCustomer();

        if (orderDTO.getDeliveryAddress() != null) {
            order.setDeliveryAddress(orderDTO.getDeliveryAddress());
        }
        if (orderDTO.getCustomerEmail() != null) {
            order.setCustomerEmail(orderDTO.getCustomerEmail());
        }
        if (orderDTO.getCustomerPhoneNumber() != null) {
            order.setCustomerPhoneNumber(orderDTO.getCustomerPhoneNumber());
        }
        if (orderDTO.getNotes() != null) {
            order.setNotes(orderDTO.getNotes());
        }
        return toDto(orderRepository.save(order));
    }

    public OrderDTO updateStatus(UUID orderId, String status) {
        UUID storeId = TenantContext.getTenantId();
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        ensureNonCustomer();

        OrderStatus nextStatus = OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        order.setStatus(nextStatus);
        if (nextStatus == OrderStatus.DELIVERED) {
            order.setCompletedAt(Instant.now().toEpochMilli());
            if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
                long releaseAt = Instant.now().plusSeconds(2 * 24 * 60 * 60L).toEpochMilli();
                order.setReleaseAt(releaseAt);
                billingTransactionRepository.findFirstByOrderIdAndStoreId(order.getId(), storeId).ifPresent(transaction -> {
                    if (transaction.getStatus() == BillingStatus.HOLD) {
                        transaction.setReleaseAt(releaseAt);
                        billingTransactionRepository.save(transaction);
                    }
                });
                try {
                    escrowService.markReleased(order.getId());
                } catch (Exception ignored) {
                    // if no escrow exists yet, let scheduled job handle future releases
                }
            }
        }
        return toDto(orderRepository.save(order));
    }

    private void ensureOrderAccess(Order order) {
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        if (principal.getRole() != UserRole.CUSTOMER) {
            return;
        }
        Customer customer = customerService.requireCurrentCustomer();
        if (!order.getCustomerId().equals(customer.getId())) {
            throw new BusinessException("ORDER_ACCESS_DENIED", "Customers can only access their own orders.");
        }
    }

    private void ensureNonCustomer() {
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        if (principal.getRole() == UserRole.CUSTOMER) {
            throw new BusinessException("ORDER_UPDATE_FORBIDDEN", "Customers cannot modify order status or internal details.");
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStoreId(UUID storeId) {
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        if (principal.getRole() == UserRole.CUSTOMER) {
            throw new BusinessException("ORDER_ACCESS_DENIED", "Customers cannot view all store orders.");
        }
        return orderRepository.findAllByStoreId(storeId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomerId(UUID customerId) {
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        // Customer can only view their own orders; staff/admin can view any
        if (principal.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerService.requireCurrentCustomer();
            if (!customer.getId().equals(customerId) && !customer.getUserId().equals(customerId)) {
                throw new BusinessException("ORDER_ACCESS_DENIED", "Customers can only access their own orders.");
            }
        }
        return orderRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
    }

    private OrderDTO toDto(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .storeId(order.getStoreId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .shippingCost(order.getShippingCost())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .status(order.getStatus() == null ? null : order.getStatus().name())
                .paymentStatus(order.getPaymentStatus() == null ? null : order.getPaymentStatus().name())
                .deliveryAddress(order.getDeliveryAddress())
                .customerEmail(order.getCustomerEmail())
                .customerPhoneNumber(order.getCustomerPhoneNumber())
                .notes(order.getNotes())
                .completedAt(order.getCompletedAt())
                .releaseAt(order.getReleaseAt())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String generateOrderNumber() {
        return "CW-MANUAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
