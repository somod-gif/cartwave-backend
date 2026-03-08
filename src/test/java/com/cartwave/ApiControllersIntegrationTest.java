package com.cartwave;

import com.cartwave.auth.dto.LoginRequest;
import com.cartwave.billing.entity.BillingStatus;
import com.cartwave.billing.entity.BillingTransaction;
import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.cart.entity.CartStatus;
import com.cartwave.cart.repository.CartRepository;
import com.cartwave.cart.repository.CartItemRepository;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.repository.CustomerRepository;
import com.cartwave.order.entity.Order;
import com.cartwave.order.entity.OrderStatus;
import com.cartwave.order.entity.PaymentStatus;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.payment.dto.PaymentConfirmRequest;
import com.cartwave.product.entity.Product;
import com.cartwave.product.entity.ProductStatus;
import com.cartwave.product.repository.ProductRepository;
import com.cartwave.security.service.JwtTokenProvider;
import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.entity.UserStatus;
import com.cartwave.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ApiControllersIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.jwt-secret", () -> "integration-secret-123456789012345678901234567890");
        registry.add("jwt.secret", () -> "integration-secret-123456789012345678901234567890");
    }

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    BillingTransactionRepository billingTransactionRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    CustomerRepository customerRepository;

    private UUID storeId;
    private User owner;
    private User customer;
    private Product product;
    private BillingTransaction transaction;
    private Order order;
    private String ownerToken;
    private String customerToken;
    private Customer customerProfile;

    @BeforeEach
    void setupData() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        billingTransactionRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        storeRepository.deleteAll();
        userRepository.deleteAll();

        owner = User.builder()
                .email("owner@test.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(UserRole.BUSINESS_OWNER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        owner = userRepository.save(owner);

        Store store = Store.builder()
                .name("Test Store")
                .slug("test-store")
                .ownerId(owner.getId())
                .currency("USD")
                .isActive(true)
                .build();
        store = storeRepository.save(store);
        this.storeId = store.getId();

        product = Product.builder()
                .storeId(store.getId())
                .name("Test Product")
                .price(BigDecimal.valueOf(50))
                .stock(100L)
                .status(ProductStatus.ACTIVE)
                .build();
        product = productRepository.save(product);

        customer = User.builder()
                .email("customer@test.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        customer = userRepository.save(customer);

        customerProfile = Customer.builder()
                .userId(customer.getId())
                .storeId(store.getId())
                .phone("123456789")
                .addressesJson("[]")
                .wishlistJson("[]")
                .build();
        customerProfile = customerRepository.save(customerProfile);

        order = Order.builder()
                .storeId(store.getId())
                .customerId(customerProfile.getId())
                .orderNumber("ORDER-" + UUID.randomUUID().toString().substring(0, 8))
                .totalAmount(BigDecimal.valueOf(75))
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        transaction = BillingTransaction.builder()
                .storeId(store.getId())
                .orderId(order.getId())
                .transactionId("txn_" + UUID.randomUUID().toString().substring(0, 6))
                .amount(order.getTotalAmount())
                .currency("USD")
                .status(BillingStatus.PENDING)
                .build();
        transaction = billingTransactionRepository.save(transaction);

        ownerToken = jwtTokenProvider.generateToken(owner, store.getId());
        customerToken = jwtTokenProvider.generateToken(customer, store.getId());
    }

    @Test
    void authControllerLoginReturnsToken() throws Exception {
        String json = """
                {
                  "email": "owner@test.com",
                  "password": "Password123!",
                  "storeId": "%s"
                }
                """.formatted(storeId);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    void cartControllerAddsItem() throws Exception {
        String body = """
                {
                  "productId": "%s",
                  "quantity": 2
                }
                """.formatted(product.getId());

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void orderControllerListsOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void paymentConfirmUpdatesTransaction() throws Exception {
        String body = """
                {
                  "transactionId": "%s",
                  "status": "SUCCESS"
                }
                """.formatted(transaction.getTransactionId());

        mockMvc.perform(post("/api/v1/payments/confirm")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value(transaction.getTransactionId()));
    }
}
