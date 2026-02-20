package com.cartwave.product.repository;

import com.cartwave.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.storeId = :storeId AND p.deleted = false")
    Optional<Product> findByIdAndStoreId(@Param("id") UUID id, @Param("storeId") UUID storeId);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND p.deleted = false")
    Page<Product> findByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.sku = :sku AND p.storeId = :storeId AND p.deleted = false")
    Optional<Product> findBySkuAndStoreId(@Param("sku") String sku, @Param("storeId") UUID storeId);

}
