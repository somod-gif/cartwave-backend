# CartWave Backend - Developer's Guide

## Adding New Modules

This guide explains how to add new features following the CartWave architecture patterns.

### Module Structure Template

Every module should follow this structure:

```
module/
├── controller/
│   └── ModuleController.java
├── service/
│   └── ModuleService.java
├── repository/
│   └── ModuleRepository.java
├── entity/
│   └── ModuleEntity.java
├── dto/
│   ├── ModuleRequestDTO.java
│   ├── ModuleResponseDTO.java
├── mapper/
│   └── ModuleMapper.java
└── (optional) exception/
    └── ModuleException.java
```

## Step-by-Step: Adding a Product Review Module

### 1. Create Entity

```java
// review/entity/ProductReview.java
@Entity
@Table(name = "product_reviews", indexes = {
    @Index(name = "idx_reviews_product_id", columnList = "product_id"),
    @Index(name = "idx_reviews_store_id", columnList = "store_id"),
    @Index(name = "idx_reviews_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductReview extends BaseEntity {

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column
    private Long reviewedAt;
}
```

### 2. Create Repository

```java
// review/repository/ProductReviewRepository.java
@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {

    @Query("SELECT r FROM ProductReview r " +
           "WHERE r.productId = :productId AND r.storeId = :storeId AND r.deleted = false")
    Page<ProductReview> findByProductIdAndStoreId(
        @Param("productId") UUID productId,
        @Param("storeId") UUID storeId,
        Pageable pageable);

    @Query("SELECT r FROM ProductReview r " +
           "WHERE r.id = :id AND r.storeId = :storeId AND r.deleted = false")
    Optional<ProductReview> findByIdAndStoreId(
        @Param("id") UUID id,
        @Param("storeId") UUID storeId);
}
```

### 3. Create DTOs

```java
// review/dto/ProductReviewDTO.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDTO {

    private UUID id;
    private UUID productId;
    private UUID customerId;
    private Integer rating;
    private String comment;
    private Boolean verified;
    private Instant createdAt;
}

// review/dto/CreateReviewRequest.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Rating is required")
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;
}
```

### 4. Create Mapper

```java
// review/mapper/ProductReviewMapper.java
@Mapper(componentModel = "spring")
public interface ProductReviewMapper {

    ProductReviewDTO toProductReviewDTO(ProductReview review);

    ProductReview toProductReview(ProductReviewDTO dto);
}
```

### 5. Create Service

```java
// review/service/ProductReviewService.java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductReviewMapper mapper;

    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getProductReviews(UUID productId, Pageable pageable) {
        log.info("Fetching reviews for product: {}", productId);
        UUID storeId = TenantContext.getTenantId();

        // Verify product exists
        productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Page<ProductReview> reviews = reviewRepository
                .findByProductIdAndStoreId(productId, storeId, pageable);

        return reviews.map(mapper::toProductReviewDTO);
    }

    public ProductReviewDTO createReview(UUID productId, CreateReviewRequest request, UUID customerId) {
        log.info("Creating review for product: {}", productId);
        UUID storeId = TenantContext.getTenantId();

        // Verify product exists
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if customer already reviewed
        // (Implement logic to prevent duplicate reviews)

        ProductReview review = ProductReview.builder()
                .storeId(storeId)
                .productId(productId)
                .customerId(customerId)
                .rating(request.getRating())
                .comment(request.getComment())
                .verified(false)
                .build();

        ProductReview saved = reviewRepository.save(review);
        log.info("Review created: {}", saved.getId());

        return mapper.toProductReviewDTO(saved);
    }
}
```

### 6. Create Controller

```java
// review/controller/ProductReviewController.java
@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ProductReviewDTO>>> getProductReviews(
            @PathVariable UUID productId,
            Pageable pageable) {
        log.info("Get product reviews endpoint called");
        Page<ProductReviewDTO> reviews = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(
            ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductReviewDTO>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        log.info("Create review endpoint called");
        UUID customerId = (UUID) authentication.getPrincipal();
        ProductReviewDTO review = reviewService.createReview(
            request.getProductId(),
            request,
            customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", review));
    }
}
```

### 7. Update Database Migration

```sql
-- V4__add_product_reviews.sql
CREATE TABLE product_reviews (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL,
    product_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    verified BOOLEAN NOT NULL DEFAULT false,
    reviewed_at BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (store_id) REFERENCES stores(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (customer_id) REFERENCES users(id)
);

CREATE INDEX idx_reviews_product_id ON product_reviews(product_id);
CREATE INDEX idx_reviews_store_id ON product_reviews(store_id);
CREATE INDEX idx_reviews_deleted ON product_reviews(deleted);
CREATE INDEX idx_reviews_product_store ON product_reviews(product_id, store_id) WHERE deleted = false;
```

## Best Practices

### 1. Always Use TenantContext for Multi-Tenancy

```java
// ✅ Correct
UUID storeId = TenantContext.getTenantId();
var items = repository.findByStoreId(storeId);

// ❌ Wrong - missing tenant filtering
var items = repository.findAll();
```

### 2. Use DTO Pattern

```java
// ✅ Correct - return DTO
public ProductDTO getProduct(UUID id) {
    Product product = repository.findById(id).orElseThrow(...);
    return mapper.toProductDTO(product);  // Return DTO
}

// ❌ Wrong - returning entity
public Product getProduct(UUID id) {
    return repository.findById(id).orElseThrow(...);  // Don't expose entity
}
```

### 3. Proper Exception Handling

```java
// ✅ Correct
try {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> 
            new ResourceNotFoundException("User", "id", userId));
} catch (Exception e) {
    log.error("Error fetching user", e);
    throw new BusinessException("USER_FETCH_ERROR", "Failed to fetch user");
}

// ❌ Wrong - generic exception
User user = userRepository.findById(userId).get();  // Can throw NoSuchElementException
```

### 4. Transactional Boundaries

```java
// ✅ Correct - service method is transactional
@Service
@Transactional
public class UserService {
    public UserDTO createUser(CreateUserRequest request) {
        User user = new User(...);
        return mapper.toUserDTO(userRepository.save(user));
    }
}

// ❌ Wrong - transaction per entity operation
@Service
public class UserService {
    @Transactional
    public void step1() { ... }
    
    @Transactional
    public void step2() { ... }
    // If step2 fails, step1 changes are not rolled back together
}
```

### 5. Constructor Injection Only

```java
// ✅ Correct
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;
}

// ❌ Wrong - field injection
@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;  // Don't use field injection
}
```

### 6. Logging Patterns

```java
// ✅ Correct
@Slf4j
@Service
public class OrderService {
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating order with {} items", request.getItems().size());
        Order order = orderRepository.save(...);
        log.info("Order created: {}", order.getId());
        return mapper.toOrderDTO(order);
    }
}

// ❌ Wrong - too verbose or missing logs
public OrderDTO createOrder(CreateOrderRequest request) {
    Order order = orderRepository.save(...);
    return mapper.toOrderDTO(order);
}
```

### 7. Query Performance

```java
// ✅ Correct - Add indexes for frequent filters
@Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND p.status = :status AND p.deleted = false")
Page<Product> findByStoreAndStatus(@Param("storeId") UUID storeId, @Param("status") ProductStatus status, Pageable pageable);

// ❌ Wrong - No pagination, can fetch huge result set
@Query("SELECT p FROM Product p WHERE p.storeId = :storeId")
List<Product> findByStore(@Param("storeId") UUID storeId);
```

### 8. API Response Consistency

```java
// ✅ Correct - always use ApiResponse
return ResponseEntity.ok(
    ApiResponse.success("Products retrieved", products));

// ❌ Wrong - inconsistent response
return ResponseEntity.ok(products);  // Missing wrapper
```

## Common Patterns

### Pattern 1: Creating Resource

```java
@PostMapping
public ResponseEntity<ApiResponse<ItemDTO>> create(@Valid @RequestBody CreateItemRequest request) {
    ItemDTO item = service.create(request, authentication);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Item created successfully", item));
}
```

### Pattern 2: Retrieving Single Resource

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ItemDTO>> get(@PathVariable UUID id) {
    ItemDTO item = service.getById(id);
    return ResponseEntity.ok(
        ApiResponse.success("Item retrieved successfully", item));
}
```

### Pattern 3: Retrieving Multiple Resources (Paginated)

```java
@GetMapping
public ResponseEntity<ApiResponse<Page<ItemDTO>>> list(Pageable pageable) {
    Page<ItemDTO> items = service.list(pageable);
    return ResponseEntity.ok(
        ApiResponse.success("Items retrieved successfully", items));
}
```

### Pattern 4: Updating Resource

```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<ItemDTO>> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateItemRequest request) {
    ItemDTO item = service.update(id, request);
    return ResponseEntity.ok(
        ApiResponse.success("Item updated successfully", item));
}
```

### Pattern 5: Deleting Resource (Soft Delete)

```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.ok(
        ApiResponse.success("Item deleted successfully"));
}
```

## Testing

### Unit Test Template

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void testGetProductById() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        ProductDTO expectedDTO = new ProductDTO();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productMapper.toProductDTO(product))
                .thenReturn(expectedDTO);

        // Act
        ProductDTO result = productService.getProductById(productId);

        // Assert
        assertEquals(expectedDTO, result);
        verify(productRepository).findById(productId);
    }
}
```

## Performance Optimization

### 1. Use @Transactional(readOnly = true)

```java
@Transactional(readOnly = true)
public Page<ProductDTO> getProducts(Pageable pageable) {
    // Optimizes database for read-only queries
    return repository.findAll(pageable).map(mapper::toProductDTO);
}
```

### 2. Lazy Loading vs Eager Loading

```java
// ✅ Correct - use @EntityGraph for specific cases
@EntityGraph(attributePaths = {"store", "category"})
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithDetails(@Param("id") UUID id);

// ❌ Wrong - always eager loading can hurt performance
@ManyToOne(fetch = FetchType.EAGER)
private Store store;
```

## Security Considerations

### 1. Always Check Tenant Access

```java
// ✅ Correct
UUID storeId = TenantContext.getTenantId();
Product product = repository.findByIdAndStoreId(productId, storeId)
    .orElseThrow(() -> new TenantAccessDeniedException(...));

// ❌ Wrong - doesn't check store ownership
Product product = repository.findById(productId).orElseThrow(...);
```

### 2. Validate User Permissions

```java
// ✅ Correct - use @PreAuthorize
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public void deleteProduct(UUID productId) { ... }

// ❌ Wrong - no permission check
public void deleteProduct(UUID productId) { ... }
```

## Troubleshooting Guide

### Issue: Changes not persisted

```
Cause: Missing @Transactional
Solution: Add @Transactional to service method
```

### Issue: Tenant isolation not working

```
Cause: Not using TenantContext
Solution: Use TenantContext.getTenantId() in all queries
```

### Issue: Performance degradation

```
Cause: N+1 query problem
Solution: Use @EntityGraph or JOIN FETCH
```

### Issue: Duplicate records

```
Cause: Missing unique constraints
Solution: Add unique constraints in migration
```

---

**Happy coding! Follow these patterns for consistency and maintainability.** 🚀
