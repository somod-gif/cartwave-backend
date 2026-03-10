package com.cartwave.product.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cartwave.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.storeId = :storeId AND p.deleted = false")
    Optional<Product> findByIdAndStoreId(@Param("id") UUID id, @Param("storeId") UUID storeId);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND p.deleted = false")
    Page<Product> findByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Product> findAllByStoreId(@Param("storeId") UUID storeId);

    @Query("SELECT p FROM Product p WHERE p.sku = :sku AND p.storeId = :storeId AND p.deleted = false")
    Optional<Product> findBySkuAndStoreId(@Param("sku") String sku, @Param("storeId") UUID storeId);

    // count current non-deleted products for a store
    long countByStoreIdAndDeletedFalse(UUID storeId);

    long countByStoreIdAndStockLessThanEqualAndDeletedFalse(UUID storeId, Long stock);

    // ── Search / filter ───────────────────────────────────────────────────────

    @Query("""
        SELECT p FROM Product p
        WHERE p.deleted = false
          AND (:storeId IS NULL OR p.storeId = :storeId)
          AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
          AND (:inStockOnly = false OR p.stock > 0)
          AND (:publishedOnly = false OR p.isPublished = true)
        ORDER BY p.createdAt DESC
    """)
    Page<Product> search(
            @Param("storeId") UUID storeId,
            @Param("q") String q,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStockOnly") boolean inStockOnly,
            @Param("publishedOnly") boolean publishedOnly,
            Pageable pageable
    );

}

