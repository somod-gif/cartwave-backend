package com.cartwave.cart.repository;

import com.cartwave.cart.entity.Cart;
import com.cartwave.cart.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("SELECT c FROM Cart c WHERE c.customerId = :customerId AND c.storeId = :storeId AND c.status = :status AND c.deleted = false")
    Optional<Cart> findByCustomerIdAndStoreIdAndStatus(
            @Param("customerId") UUID customerId,
            @Param("storeId") UUID storeId,
            @Param("status") CartStatus status
    );
}
