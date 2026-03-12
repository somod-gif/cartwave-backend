package com.cartwave.product.repository;

import com.cartwave.product.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdAndDeletedFalse(UUID productId, Pageable pageable);

    Optional<Review> findByProductIdAndCustomerIdAndDeletedFalse(UUID productId, UUID customerId);

    boolean existsByProductIdAndCustomerIdAndDeletedFalse(UUID productId, UUID customerId);
}
