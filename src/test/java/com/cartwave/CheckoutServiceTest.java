package com.cartwave;

import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.cart.entity.Cart;
import com.cartwave.cart.entity.CartStatus;
import com.cartwave.cart.service.CartService;
import com.cartwave.checkout.dto.CheckoutRequest;
import com.cartwave.checkout.service.CheckoutService;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.service.CustomerService;
import com.cartwave.exception.BusinessException;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    CartService cartService;
    @Mock
    CustomerService customerService;
    @Mock
    OrderRepository orderRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    BillingTransactionRepository billingTransactionRepository;

    @InjectMocks
    CheckoutService checkoutService;

    @Test
    void checkoutRejectsEmptyCart() {
        Customer customer = Customer.builder()
                .storeId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .build();
        customer.setId(UUID.randomUUID());
        Cart cart = Cart.builder()
                .storeId(customer.getStoreId())
                .customerId(customer.getId())
                .status(CartStatus.ACTIVE)
                .subtotal(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .currency("USD")
                .build();
        cart.setId(UUID.randomUUID());

        when(customerService.requireCurrentCustomer()).thenReturn(customer);
        when(cartService.getOrCreateActiveCart()).thenReturn(cart);
        when(cartService.getActiveItems(cart)).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> checkoutService.checkout(new CheckoutRequest()));
    }
}
