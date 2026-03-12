package com.cartwave.product.repository;

import com.cartwave.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProductIdAndDeletedFalse(UUID productId);

    Optional<ProductVariant> findByIdAndDeletedFalse(UUID id);
}
