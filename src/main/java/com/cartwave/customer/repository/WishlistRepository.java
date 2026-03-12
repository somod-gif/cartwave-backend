package com.cartwave.customer.repository;

import com.cartwave.customer.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    List<Wishlist> findByCustomerIdAndStoreIdAndDeletedFalse(UUID customerId, UUID storeId);

    Optional<Wishlist> findByCustomerIdAndProductIdAndDeletedFalse(UUID customerId, UUID productId);

    boolean existsByCustomerIdAndProductIdAndDeletedFalse(UUID customerId, UUID productId);
}
